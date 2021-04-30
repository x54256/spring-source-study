package cn.x5456.rs.pre;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.x5456.rs.pre.def.BigFileUploader;
import cn.x5456.rs.pre.def.IResourceStorage;
import cn.x5456.rs.pre.def.UploadProgress;
import cn.x5456.rs.pre.document.FileMetadata;
import cn.x5456.rs.pre.document.FsFileTemp;
import cn.x5456.rs.pre.document.FsResourceInfo;
import com.google.common.annotations.Beta;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.mongodb.client.result.DeleteResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author yujx
 * @date 2021/04/26 09:20
 */
@Slf4j
@Component
// 配合 @ConditionalOnClass(Mongo.class)
@SuppressWarnings("UnstableApiUsage")
public class MongoResourceStorage implements IResourceStorage {

    /**
     * 256 KB
     * {@see org.springframework.data.mongodb.gridfs.ReactiveGridFsResource.DEFAULT_CHUNK_SIZE}
     */
    static final Integer DEFAULT_CHUNK_SIZE = 256 * 1024;

    private static final String TMP_SUFFIX = ".tmp";

    /**
     * 正式的后缀
     */
    private static final String OFFICIAL_SUFFIX = ".official";

    private static final String LOCAL_TEMP_PATH;

    static {
        LOCAL_TEMP_PATH = System.getProperty("java.io.tmpdir") + File.separator + "cn.x5456.rs" + File.separator;
        FileUtil.mkdir(LOCAL_TEMP_PATH);

        // 每次重启清理未"转正"的文件
        File[] ls = FileUtil.ls(LOCAL_TEMP_PATH);
        for (File file : ls) {
            if (file.isFile()) {
                String suffix = "." + FileUtil.getSuffix(file);
                if (suffix.equals(TMP_SUFFIX)) {
                    FileUtil.del(file);
                }
            }
        }
    }

    /**
     * 布隆过滤器，过滤重复请求 todo 配合 hash 环
     */
    private final BloomFilter<CharSequence> bloomFilter = BloomFilter.create(
            // Funnel 预估元素个数 误判率
            Funnels.stringFunnel(Charset.defaultCharset()), 1024, 0.01);

    /**
     * 构建一个 LRU 缓存实例 todo 配合 hash 环
     */
    private final Cache<String, FileMetadata> cache = CacheBuilder.newBuilder()
            .maximumSize(100) // 设置缓存的最大容量
            .concurrencyLevel(10) // 设置并发级别为 10
            .recordStats() // 开启缓存统计
            .build();

    private final BigFileUploader INSTANCE = new BigFileUploaderImpl();

    private final DataBufferFactory dataBufferFactory;

    private final ReactiveMongoTemplate mongoTemplate;

    private final ReactiveGridFsTemplate gridFsTemplate;

    private final Scheduler scheduler;

    public MongoResourceStorage(DataBufferFactory dataBufferFactory, ReactiveMongoTemplate mongoTemplate,
                                ReactiveGridFsTemplate gridFsTemplate, ObjectProvider<Scheduler> schedulerObjectProvider) {
        this.dataBufferFactory = dataBufferFactory;
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
        this.scheduler = schedulerObjectProvider.getIfUnique(Schedulers::elastic);
    }

    /**
     * 上传文件到文件服务
     *
     * @param localFilePath 本地文件路径
     * @param path          服务上存储的标识
     * @return 是否上传成功
     */
    @Override
    public Mono<FsResourceInfo> uploadFile(String localFilePath, String path) {
        String fileName = FileUtil.getName(localFilePath);
        return this.uploadFile(localFilePath, fileName, path);
    }

    /**
     * 上传文件到文件服务
     * todo 要是上传了一半线程挂了，或者服务器挂了怎么办，状态已经不会改变了但他又在那占着坑
     * 策略：
     * 1. 定时任务监测 metadata 和 temp 表，当其超过 mongo 连接超时时间 * 2 的时候，则记录日志并删除
     * 2. redis 过期键提醒，可以检测是否引入 redis，如果引入默认用这个。
     *
     * @param localFilePath 本地文件路径
     * @param fileName      文件名
     * @param path          服务上存储的标识
     * @return 是否上传成功
     */
    @Override
    public Mono<FsResourceInfo> uploadFile(String localFilePath, String fileName, String path) {
        FileSystemResource resource = new FileSystemResource(localFilePath);
        Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(resource, dataBufferFactory, DEFAULT_CHUNK_SIZE);
        return this.uploadFile(dataBufferFlux, fileName, path);
    }

    /**
     * dataBufferFlux 方式上传文件到文件服务
     *
     * @param dataBufferFlux dataBufferFlux
     * @param fileName       文件名
     * @param path           服务上存储的标识
     * @return 是否上传成功
     */
    @Override
    public Mono<FsResourceInfo> uploadFile(Flux<DataBuffer> dataBufferFlux, String fileName, String path) {
        return this.calcFileHashCode(dataBufferFlux)
                .flatMap(fileHash -> this.getFileMetadata(fileHash)
                        .switchIfEmpty(this.doUploadFile(fileHash, dataBufferFlux))
                        .flatMap(m -> this.insertResource(m, fileName, path)));
    }

    @NotNull
    private Mono<String> calcFileHashCode(Flux<DataBuffer> dataBufferFlux) {
        return Mono.create(sink -> {
            // 计算文件的 hash 值
            // main
            MessageDigest digest = SecureUtil.createMessageDigest(DigestAlgorithm.SHA256.getValue());
            // data buffer thread
            dataBufferFlux
                    .doOnNext(dataBuffer -> digest.update(dataBuffer.asByteBuffer()))
                    .doOnComplete(() -> {
                        byte[] data = digest.digest();
                        String hex = HexUtil.encodeHexStr(data);
                        sink.success(hex);
                    }).subscribe();
        });
    }

    @NotNull
    private Mono<FsResourceInfo> insertResource(FileMetadata metadata, String fileName, String path) {
        FsResourceInfo fsResourceInfo = new FsResourceInfo();
        fsResourceInfo.setId(path);
        fsResourceInfo.setFileName(fileName);
        fsResourceInfo.setFileHash(metadata.getFileHash());
        fsResourceInfo.setMetadataId(metadata.getId());

        return mongoTemplate.insert(fsResourceInfo);
    }

    @NotNull
    private Mono<FileMetadata> getFileMetadata(String fileHash) {
        return mongoTemplate.findOne(Query.query(Criteria.where(FileMetadata.FILE_HASH).is(fileHash)), FileMetadata.class);
    }

    @NotNull
    private Mono<FileMetadata> doUploadFile(String fileHash, Flux<DataBuffer> dataBufferFlux) {
        return Mono.create(sink -> {
            // 尝试保存文件元数据信息
            this.insertFileMetadata(fileHash, false)
                    // 如果保存失败，则证明数据库中已经有了 hash 值为 fileHash 的数据，那么取出并返回
                    .doOnError(DuplicateKeyException.class, ex -> this.getFileMetadata(fileHash).subscribe(sink::success))
                    // 如果保存成功，则上传文件，完善文件元数据信息
                    .flatMap(m -> gridFsTemplate.store(dataBufferFlux, fileHash)
                            .doOnError(ex -> {
                                // 如果上传时遇到错误，则删除当前上传文件的元数据，留给下一次上传
                                mongoTemplate.remove(m);
                                sink.error(ex);
                            })
                            .flatMap(objectId -> {
                                m.setUploadProgress(UploadProgress.UPLOAD_COMPLETED);
                                m.setTotalNumberOfChunks(1);
                                m.setFsFilesInfoList(Lists.newArrayList(
                                        new FileMetadata.FsFilesInfo(0, objectId.toHexString(), this.getChunkSize(dataBufferFlux))));
                                return mongoTemplate.save(m);
                            })).subscribe(sink::success);
        });
    }

    @NotNull
    private Mono<FileMetadata> insertFileMetadata(String fileHash, boolean multipartUpload) throws DuplicateKeyException {
        // main
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(fileHash);
        fileMetadata.setFileHash(fileHash);
        fileMetadata.setUploadProgress(UploadProgress.UPLOADING);
        fileMetadata.setCreatTime(LocalDateTime.now());
        fileMetadata.setMultipartUpload(multipartUpload);

        // reactive mongo thread
        // 尝试保存文件元数据信息
        return mongoTemplate.insert(fileMetadata);
    }


    private Long getChunkSize(Flux<DataBuffer> dataBufferFlux) {
        return dataBufferFlux.reduce(0L, (a, b) -> a += b.readableByteCount()).block();
    }

    /**
     * 从文件服务下载文件
     *
     * @param localFilePath 本地文件路径
     * @param path          服务上存储的标识
     * @return 是否下载成功
     */
    @Override
    public Mono<Boolean> downloadFile(String localFilePath, String path) {
        return this.getResourceInfo(path)
                .switchIfEmpty(Mono.error(new RuntimeException(StrUtil.format("输入的 path：「{}」不正确！", path))))
                .flatMap(r -> {
                    String fileHash = r.getFileHash();
                    return this.download(fileHash).flatMap(srcPath -> {
                        if (!this.createSymbolicLink(srcPath, localFilePath)) {
                            try {
                                FileUtil.copyFile(srcPath, localFilePath, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IORuntimeException e) {
                                return Mono.error(e);
                            }
                        }
                        return Mono.just(true);
                    });
                });
    }

    private boolean createSymbolicLink(String sourceFile, String linkFilePath) {
        try {
            Files.createSymbolicLink(FileSystems.getDefault().getPath(linkFilePath), FileSystems.getDefault().getPath(sourceFile));
            if (FileUtil.exist(linkFilePath)) {
                return true;
            }
        } catch (IOException e) {
            log.error("异常为：", e);
        }
        return false;
    }

    @NotNull
    private Mono<String> download(String fileHash) {
        return Mono.create(sink -> {
            // 先从缓存中获取，节省一次查询
            FileMetadata metadata = cache.getIfPresent(fileHash);
            if (Objects.nonNull(metadata)) {
                // 拼接本地缓存路径，格式：缓存目录/hashcode.official
                String officialPath = this.getFileOfficialPath(metadata.getFileHash());
                if (FileUtil.exist(officialPath)) {
                    sink.success(officialPath);
                    return;
                }
            }

            this.getReadyMetadata(fileHash)
                    .subscribe(m -> {
                        // 拼接本地缓存路径，格式：缓存目录/hashcode.official
                        String officialPath = this.getFileOfficialPath(m.getFileHash());
                        if (FileUtil.exist(officialPath)) {
                            sink.success(officialPath);
                        } else {
                            log.info("officialPath：「{}」", officialPath);
                            // 2021/4/28 why????? 为啥要新开一个线程（这块是个死锁）
                            // 假设当前代码运行的线程为 N2-2，我们进入 doDownload() 方法，里面有一个循环，也是使用 N2-2 线程发送两个请求
                            // （应该是做了判断，判断当前线程是不是 EventLoopGroup 中的线程，如果不是才会进行线程的切换），可能 mongo 内部
                            // 有一个机制就是请求线程与接收线程绑定，即第一个请求用 N2-2 接收，第二个请求用 N2-3 接收，因为我们 for 循环之后
                            // 调用了 latch.await(); 将 N2-2 阻塞住了，所以当消息来了之后 N2-2 无法接收，所以程序一直无法停止。
                            // 所以，我们不能让 nio 线程阻塞，那就需要在调用时重新创建一个线程了。
                            scheduler.schedule(() -> this.doDownload(m).subscribe(localTempPath -> {
                                cache.put(localTempPath, m);
                                sink.success(localTempPath);
                            }));
                        }
                    });
        });
    }

    private String getFileOfficialPath(String fileHash) {
        return LOCAL_TEMP_PATH + fileHash + OFFICIAL_SUFFIX;
    }

    // TODO: 2021/4/30 这个方法可以改成 repeatWhenEmpty 多重试几次要是还不行那就抛异常，让他等会
    @NotNull
    private Mono<FileMetadata> getReadyMetadata(String fileHash) {
        int randomInt = RandomUtil.randomInt(100, 4000);
        return Mono.create(sink -> {
            Disposable disposable = scheduler.schedulePeriodically(() -> {
                this.getFileMetadata(fileHash)
                        .switchIfEmpty(Mono.error(new RuntimeException(StrUtil.format("hash 值为「{}」的文件元数据不存在！", fileHash))))
                        .doOnError(sink::error)
                        .subscribe(metadata -> {
                            if (metadata.getUploadProgress().equals(UploadProgress.UPLOAD_COMPLETED)) {
                                sink.success(metadata);
                            }
                        });
            }, 0, randomInt, TimeUnit.MILLISECONDS);
            // 当有数据之后停止定时任务
            sink.onDispose(disposable);
        });
    }

    @NotNull
    private Mono<String> doDownload(FileMetadata metadata) {
        return Mono.create(sink -> {

            /*
            0. 如果 tmp 文件存在
            1. 检测 hashcode.tmp 是否被删除
            2. 如果被删除则检查 hashcode.official 是否被创建出来了
            3. 如果没有，则继续下载

            虽然我觉得这样写不好，但也想不出别的办法，暂时先这样吧
             */
            String fileTempPath = this.getFileTempPath(metadata.getFileHash());
            if (FileUtil.exist(fileTempPath)) {
                int randomInt = RandomUtil.randomInt(100, 4000);

                while (true) {
                    if (!FileUtil.exist(fileTempPath)) {
                        String officialPath = this.getFileOfficialPath(metadata.getFileHash());
                        if (FileUtil.exist(officialPath)) {
                            sink.success(officialPath);
                            return;
                        } else {
                            break;
                        }
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(randomInt);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }


            // 获取每一片的信息，排序
            List<FileMetadata.FsFilesInfo> fsFilesInfoList = metadata.getFsFilesInfoList();
            fsFilesInfoList.sort(Comparator.comparingInt(FileMetadata.FsFilesInfo::getChunk));

            String tempPath = this.getFileTempPath(metadata.getFileHash());
            long index = 0;
            CountDownLatch latch = new CountDownLatch(fsFilesInfoList.size());
            AtomicBoolean isSuccess = new AtomicBoolean(true);
            try {
                for (FileMetadata.FsFilesInfo fsFilesInfo : fsFilesInfoList) {
                    RandomAccessFile randomAccessFile = new RandomAccessFile(tempPath, "rw");
                    randomAccessFile.seek(index);
                    index += fsFilesInfo.getChunkSize();

                    // 开始下载
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fsFilesInfo.getFsFilesId())))
                            .log()
                            .flatMap(gridFsTemplate::getResource)
                            .map(ReactiveGridFsResource::getDownloadStream)
                            .flux()
                            .flatMap(dataBufferFlux -> DataBufferUtils.write(dataBufferFlux, randomAccessFile.getChannel()))
                            .onErrorResume(ex -> {
                                isSuccess.set(false);
                                sink.error(ex);
                                return Mono.empty();
                            }) // TODO: 2021/4/30 没测试，不知道好不好使
                            .doOnTerminate(() -> {
                                try {
                                    randomAccessFile.close();
                                } catch (IOException e) {
                                    sink.error(e);
                                } finally {
                                    latch.countDown();
                                }
                            }).subscribe();
                }

                // 注释掉，在调用方加个 sleep 就可以看到正常接收了 onNext()
                latch.await();

                if (isSuccess.get()) {
                    // 文件更名（"转正"）
                    File officialFile = FileUtil.rename(
                            new File(tempPath), metadata.getFileHash() + OFFICIAL_SUFFIX, true);
                    sink.success(officialFile.getPath());
                } else {
                    // 下载失败，删除缓存文件
                    FileUtil.del(tempPath);
                }
            } catch (Exception e) {
                sink.error(e);
            }
        });
    }

    private String getFileTempPath(String fileHash) {
        return LOCAL_TEMP_PATH + fileHash + TMP_SUFFIX;
    }

    /**
     * 从文件服务中获取文件
     *
     * @param path 服务上存储的标识
     * @return Pair key-文件名，value-dataBufferFlux
     */
    @Override
    public Mono<Pair<String, Flux<DataBuffer>>> downloadFileDataBuffer(String path) {
        return this.getResourceInfo(path)
                .switchIfEmpty(Mono.error(new RuntimeException(StrUtil.format("输入的 path：「{}」不正确！", path))))
                .flatMap(r -> {
                    String fileHash = r.getFileHash();
                    String fileName = r.getFileName();

                    return this.download(fileHash).map(localFilePath -> {
                        Flux<DataBuffer> read = DataBufferUtils.read(new FileSystemResource(localFilePath), dataBufferFactory, DEFAULT_CHUNK_SIZE);
                        return new Pair<>(fileName, read);
                    });
                });
    }

    @NotNull
    private Mono<FsResourceInfo> getResourceInfo(String path) {
        return mongoTemplate.findOne(Query.query(Criteria.where(FsResourceInfo.PATH).is(path)), FsResourceInfo.class);
    }

    /**
     * 删除文件服务上的文件
     * todo（引用计数）
     *
     * @param path 服务上存储的标识
     * @return 是否删除成功
     */
    @Override
    public Mono<Boolean> deleteFile(String path) {
        Mono<DeleteResult> removeMono = mongoTemplate.remove(this.getResourceInfo(path));
        return removeMono.map(DeleteResult::wasAcknowledged);
    }

    /**
     * 通过path获取文件名
     *
     * @param path 服务上存储的标识
     * @return 文件名【包括后缀】
     */
    @Override
    public Mono<String> getFileName(String path) {
        return this.getResourceInfo(path).map(FsResourceInfo::getFileName);
    }

    @Override
    public BigFileUploader getBigFileUploader() {
        return INSTANCE;
    }

    /**
     * 清除本地所有缓存
     */
    public void cleanLocalTemp() {
        FileUtil.del(LOCAL_TEMP_PATH);
        FileUtil.mkdir(LOCAL_TEMP_PATH);
    }

    // TODO: 2021/4/30 定时删除访问量低的文件，通过 LRU 缓存

    class BigFileUploaderImpl implements BigFileUploader {

        /**
         * 是否已经存在（秒传）
         *
         * @param fileHash 文件 hash
         * @return 文件是否已在服务中存在
         */
        @Override
        public Mono<Boolean> secondPass(String fileHash) {
            return MongoResourceStorage.this.getFileMetadata(fileHash).map(m -> true)
                    // 查不到数据时返回的是一个空的 Mono，并不会调用 map()，所以需要使用 defaultIfEmpty 返回值
                    .defaultIfEmpty(false);
        }

        /**
         * 上传每一片文件
         *
         * @param fileHash       文件 hash
         * @param chunk          第几片
         * @param dataBufferFlux 当前片的 dataBufferFlux
         * @return 是否上传成功，上传成功返回 true，当发现已经有其他线程上传过了返回 false
         * @throws RuntimeException 上传文件时失败
         */
        @Override
        public Mono<Boolean> uploadFileChunk(String fileHash, int chunk, Flux<DataBuffer> dataBufferFlux) {
            /*
            1. 检查文件 hash 是否存在
            2. 如果不存在，则在 fs.metadata 表创建一个
            3. 如果创建失败，则证明存在，则从 fs.metadata 中取出

            4. 去 fs.temp 表创建当前分片的缓存
            5. 如果创建成功，则执行上传逻辑，保存到 fs.temp 表中，返回 true
            6. 如果创建失败，则证明已经有了一个线程抢先上传了，返回 false
             */

            return MongoResourceStorage.this.getFileMetadata(fileHash)
                    .switchIfEmpty(this.createOrGet(fileHash))
                    .flatMap(metadata -> this.insertChunkTempInfoV2(fileHash, chunk))
                    .flatMap(temp -> gridFsTemplate.store(dataBufferFlux, fileHash + "_" + chunk)
                            .doOnError(ex -> {
                                // 上传失败，删除缓存表信息
                                mongoTemplate.remove(temp);
                            })
                            .flatMap(objectId -> {
                                temp.setFsFilesId(objectId.toHexString());
                                temp.setUploadProgress(UploadProgress.UPLOAD_COMPLETED);
                                temp.setChunkSize(MongoResourceStorage.this.getChunkSize(dataBufferFlux));
                                return mongoTemplate.save(temp);
                            }))
                    .map(fsFileTemp -> true)
                    /*
                    当遇到 DuplicateKeyException 异常时说明已经有一个线程在上传了，所以停止上传，返回 false
                    当流为空的时候，说明被布隆过滤器过滤掉了，所以也返回 false
                     */
                    .onErrorReturn(DuplicateKeyException.class, false)
                    .defaultIfEmpty(false);
        }

        private Mono<FileMetadata> createOrGet(String fileHash) {
            // 尝试保存文件元数据信息
            return MongoResourceStorage.this.insertFileMetadata(fileHash, true)
                    // 如果保存失败，则证明数据库中已经有了 hash 值为 fileHash 的数据，那么取出并返回
                    .onErrorResume(DuplicateKeyException.class, ex -> MongoResourceStorage.this.getFileMetadata(fileHash));
        }

        // 方案二：为他计算出一个唯一的 id(hash 算法) + 把 save 换成 insert
        // 2021/4/29 小想法，把 id 设置成一样的呢？ 会不会把 id 的索引删了 -> 测试结果，不会
        private Mono<FsFileTemp> insertChunkTempInfoV2(String fileHash, int chunk) throws DuplicateKeyException {
            String key = fileHash + "_" + chunk;

            // TODO: 2021/4/30 下面这个先查询在添加的动作是线程不安全的，但也不想加锁了，等他们以后 api 支持吧
            // 也可以使用布隆过滤器过滤下，毕竟下面这个动作还是比较重的，最好保证他能成功
            if (bloomFilter.mightContain(key)) {
                log.info("布隆过滤器过滤的 key 重复：「{}」", key);
                // 2021/4/29 怎样不用抛出异常的这种方式进行流的转变 -> 返回一个空对象，最后 switchIfEmpty
                return Mono.empty();
            }
            // 否则将当前 key 添加进去
            bloomFilter.put(key);

            // 尝试添加一条记录
            FsFileTemp fsFileTemp = new FsFileTemp();
            fsFileTemp.setId(SecureUtil.sha256(key));
            fsFileTemp.setFileHash(fileHash);
            fsFileTemp.setChunk(chunk);
            fsFileTemp.setUploadProgress(UploadProgress.UPLOADING);
            fsFileTemp.setCreatTime(LocalDateTime.now());
            return mongoTemplate.insert(fsFileTemp);
        }

        // 方案一：布隆过滤器 + 小延时
        // 如果两个线程同时调用这个方法保存相同的数据，那么会同时插入两条数据（具体原因暂不清楚）同时打破唯一索引约束，即删掉唯一索引。
        // 如果集合已经包含了违反索引的唯一约束的数据，MongoDB不能在指定的索引字段上创建一个唯一索引。 —— 官方文档
        // 2021/4/29 所以在这里随机等待一段时间，降低出现这种情况的概率（目前概率还是很高）。当然不是万全之策，有好的想法可以 fix me
        @Beta
        @Deprecated
        private Mono<FsFileTemp> saveChunkTempInfo(String fileHash, int chunk) throws DuplicateKeyException {

            /*
            Beta（不稳定）

            1. 布隆过滤器过滤每个实例多个相同的上传请求
            2. 随机延迟解决集群时多个相同的上传请求
            3. todo 配合 hash 环算法，将相同 hash 的转发到相同的服务器，降低不同服务器相同上传请求造成的问题
             */

            // 通过布隆过滤器来降低单实例时出现问题的几率
            String key = fileHash + "_" + chunk;
            if (bloomFilter.mightContain(key)) {
                log.info("布隆过滤器过滤的 key 重复：「{}」", key);
                // TODO: 2021/4/29 怎样不用抛出异常的这种方式进行流的转变
                return Mono.error(new DuplicateKeyException("测试键重复"));
            }

            // 否则将当前 key 添加进去
            bloomFilter.put(key);
            return Mono.create(sink -> {
                int randomInt = RandomUtil.randomInt(0, 400);
                Disposable disposable = scheduler.schedule(() -> {
                    // 尝试添加一条记录
                    FsFileTemp fsFileTemp = new FsFileTemp();
                    fsFileTemp.setFileHash(fileHash);
                    fsFileTemp.setChunk(chunk);
                    fsFileTemp.setUploadProgress(UploadProgress.UPLOADING);
                    mongoTemplate.save(fsFileTemp)
                            .doOnError(sink::error)
                            .subscribe(sink::success);

                }, randomInt, TimeUnit.MILLISECONDS);
                sink.onDispose(disposable);
            });
        }

        /**
         * 获取上传进度 {1: 上传完成， 0: 上传中，2：失败}
         *
         * @param fileHash 文件 hash
         * @return 上传进度
         */
        @Override
        public Flux<Pair<Integer, UploadProgress>> uploadProgress(String fileHash) {
            Criteria criteria = Criteria.where(FsFileTemp.FILE_HASH).is(fileHash);
            return mongoTemplate.find(Query.query(criteria), FsFileTemp.class)
                    .map(temp -> new Pair<>(temp.getChunk(), temp.getUploadProgress()));
        }

        /**
         * 全部上传完成，该接口具有幂等性
         *
         * @param fileHash            文件 hash
         * @param fileName            文件名
         * @param totalNumberOfChunks 当前文件一共多少片
         * @param path                服务上存储的标识
         * @return 操作是否成功
         */
        @Override
        public Mono<Boolean> uploadCompleted(String fileHash, String fileName, int totalNumberOfChunks, String path) {
            /*
            1. 校验传入的片数是否与系统中的片数相同
            2. 根据 fileHash 查出元数据信息，完善元数据信息
            3. 在 fs.resource 添加引用
            4. 删除缓存表数据
             */


            // 查询上传成功的
            Criteria criteria = Criteria.where(FsFileTemp.FILE_HASH).is(fileHash)
                    .and(FsFileTemp.UPLOAD_PROGRESS).is(UploadProgress.UPLOAD_COMPLETED);
            Query query = Query.query(criteria);

            return mongoTemplate.find(query, FsFileTemp.class)
                    .switchIfEmpty(Mono.error(new RuntimeException("输入的 fileHash 有误或者已经上传完成！")))
                    .collectList()
                    .flatMap(tempList -> {
                                if (tempList.size() != totalNumberOfChunks) {
                                    return Mono.error(new RuntimeException("传入的片数与服务器中的片数不符，请检查或稍后再试！"));
                                }

                                return MongoResourceStorage.this.getFileMetadata(fileHash)
                                        // 根据 fileHash 查出元数据信息，完善元数据信息
                                        .flatMap(metadata -> {
                                            metadata.setTotalNumberOfChunks(totalNumberOfChunks);
                                            metadata.setFsFilesInfoList(
                                                    tempList.stream().map(x -> BeanUtil.copyProperties(x, FileMetadata.FsFilesInfo.class)).collect(Collectors.toList()));
                                            metadata.setUploadProgress(UploadProgress.UPLOAD_COMPLETED);
                                            return mongoTemplate.save(metadata);
                                        })
                                        // 在 fs.resource 添加引用
                                        .flatMap(metadata -> MongoResourceStorage.this.insertResource(metadata, fileName, path))
                                        // 删除缓存表数据 todo 原子性，改为单独开一个线程删除吧，不管他成不成功了
                                        // todo 不过支持事务了 since 2.2. Use {@code @Transactional} or {@link TransactionalOperator}.
                                        // todo 感觉还是改成事务吧
                                        .flatMap(r -> this.cleanTemp(fileHash));
                            }
                    );
        }

        /**
         * 上传失败，清理缓存表
         *
         * @param fileHash 文件 hash
         * @return 操作是否成功
         */
        @Override
        public Mono<Boolean> uploadError(String fileHash) {
            return this.cleanTemp(fileHash);
        }

        @NotNull
        private Mono<Boolean> cleanTemp(String fileHash) {
            // 清理缓存表
            Criteria criteria = Criteria.where(FsFileTemp.FILE_HASH).is(fileHash);
            return mongoTemplate.find(Query.query(criteria), FsFileTemp.class)
                    .flatMap(mongoTemplate::remove)
                    .collectList()
                    .map(deleteResults -> true);
        }
    }

}
