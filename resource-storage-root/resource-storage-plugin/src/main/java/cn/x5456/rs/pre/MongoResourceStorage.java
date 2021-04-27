package cn.x5456.rs.pre;

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
import cn.x5456.rs.pre.document.FsResourceInfo;
import com.google.common.collect.Lists;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
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

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author yujx
 * @date 2021/04/26 09:20
 */
@Slf4j
@Component
public class MongoResourceStorage implements IResourceStorage {

    /**
     * 256 KB
     * {@see org.springframework.data.mongodb.gridfs.ReactiveGridFsResource.DEFAULT_CHUNK_SIZE}
     */
    private static final Integer DEFAULT_CHUNK_SIZE = 256 * 1024;

    // TODO: 2021/4/26 后缀改为推测
    private static final String SUFFIX = ".tmp";

    private static final String LOCAL_TEMP_PATH = System.getProperty("java.io.tmpdir");

    private DataBufferFactory dataBufferFactory;

    private ReactiveMongoTemplate mongoTemplate;

    private ReactiveGridFsTemplate gridFsTemplate;

    private GridFSBucket gridFSBucket;

    private Scheduler scheduler;

    public MongoResourceStorage(DataBufferFactory dataBufferFactory, ReactiveMongoTemplate mongoTemplate,
                                ReactiveGridFsTemplate gridFsTemplate, GridFSBucket gridFSBucket,
                                ObjectProvider<Scheduler> schedulerObjectProvider) {
        this.dataBufferFactory = dataBufferFactory;
        this.mongoTemplate = mongoTemplate;
        this.gridFsTemplate = gridFsTemplate;
        this.gridFSBucket = gridFSBucket;
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
            // main
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileHash(fileHash);
            fileMetadata.setStatus(FileMetadata.Status.初始状态);

            // reactive mongo thread
            // 尝试保存文件元数据信息
            mongoTemplate.save(fileMetadata)
                    // 如果保存失败，则证明数据库中已经有了 hash 值为 fileHash 的数据，那么取出并返回
                    .doOnError(DuplicateKeyException.class, ex -> this.getFileMetadata(fileHash).subscribe(sink::success))
                    // 如果保存成功，则上传文件，完善文件元数据信息
                    .flatMap(m -> gridFsTemplate.store(dataBufferFlux, fileHash + SUFFIX)
                            .flatMap(objectId -> {
                                // TODO: 2021/4/26 改为支持多个片的，不知道需不需要这样改 -》 不用改
                                m.setStatus(FileMetadata.Status.文件上传成功);
                                m.setTotalNumberOfChunks(1);
                                m.setFsFilesInfoList(Lists.newArrayList(
                                        new FileMetadata.FsFilesInfo(0, objectId.toHexString(), this.getChunkSize(dataBufferFlux))));
                                return mongoTemplate.save(m);
                            })).subscribe(sink::success);
        });
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

    @NotNull
    private Mono<FileMetadata> getReadyMetadata(String fileHash) {
        int randomInt = RandomUtil.randomInt(100, 4000);
        return Mono.create(sink -> {
            Disposable disposable = scheduler.schedulePeriodically(() -> {
                this.getFileMetadata(fileHash)
                        .switchIfEmpty(Mono.error(new RuntimeException(StrUtil.format("hash 值为「{}」的文件元数据不存在！", fileHash))))
                        .doOnError(sink::error)
                        .subscribe(metadata -> {
                            if (metadata.getStatus().equals(FileMetadata.Status.文件上传成功)) {
                                sink.success(metadata);
                            }
                        });
            }, 0, randomInt, TimeUnit.MILLISECONDS);
            // 当有数据之后停止定时任务
            sink.onDispose(disposable);
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

    // 这不还是回调地狱吗，看看有没有啥办法解决，then 那个是干啥的
    // then 好像确实可以串起来，但是我们这个需要入参啊
    // 可以用 flatMap 串起来
    @NotNull
    private Mono<String> download(String fileHash) {
        return this.getReadyMetadata(fileHash)
                .flatMap(m -> {
                    // 拼接本地缓存路径，格式：缓存目录/hashcode.tmp
                    String tempPath = LOCAL_TEMP_PATH + m.getFileHash() + SUFFIX;
                    return FileUtil.exist(tempPath) ? Mono.just(tempPath) : this.doDownload(m, tempPath);
                });
    }

    @NotNull
    private Mono<String> doDownload(FileMetadata metadata, String tempPath) {
        return Mono.create(sink -> {
            // TODO: 2021/4/27 多片的
            FileMetadata.FsFilesInfo fsFilesInfo = metadata.getFsFilesInfoList().get(0);
            gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fsFilesInfo.getFsFilesId())))
                    .flatMap(gridFsTemplate::getResource)
                    .map(ReactiveGridFsResource::getDownloadStream)
                    .flatMap(dataBufferFlux -> DataBufferUtils.write(dataBufferFlux.log(),
                            Paths.get(tempPath), StandardOpenOption.WRITE, StandardOpenOption.CREATE))
                    .doOnSuccess(x -> sink.success(tempPath))
                    .subscribe();
        });
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
    public BigFileUploader getBigFileUploader(String fileHash) {
        return null;
    }

    class BigFileUploaderImpl implements BigFileUploader {

        /**
         * 是否已经存在（秒传）
         *
         * @param fileHash 文件 hash
         * @return 文件是否已在服务中存在
         */
        @Override
        public Mono<Boolean> secondPass(String fileHash) {
            return MongoResourceStorage.this.getFileMetadata(fileHash).map(Objects::nonNull);
        }

        /**
         * 上传每一片文件
         * 1. 检查 hash 是否存在，避免重复上传
         * 2. 上传时检查上传进度，如果已经在上传那就不上传了
         *
         * @param fileHash       文件 hash
         * @param chunk          第几片
         * @param dataBufferFlux 当前片的 dataBufferFlux
         * @return 是否上传成功
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


            return null;
        }

        /**
         * 获取上传进度 {1: 上传完成， 0: 上传中，2：失败}  1  2  4  8  与运算
         *
         * @param fileHash 文件 hash
         * @return 上传进度
         */
        @Override
        public Flux<Pair<Integer, UploadProgress>> uploadProgress(String fileHash) {
            return null;
        }

        /**
         * 全部上传完成（文件名，总块数"用来做校验"），有什么办法保证数据无法被更改
         *
         * @param fileHash            文件 hash
         * @param fileName            文件名
         * @param totalNumberOfChunks 当前文件一共多少片
         * @param path                服务上存储的标识
         * @return 操作是否成功
         */
        @Override
        public Mono<Boolean> uploadCompleted(String fileHash, String fileName, int totalNumberOfChunks, String path) {
            return null;
        }

        /**
         * 上传失败，清理
         *
         * @param fileHash 文件 hash
         * @return void
         */
        @Override
        public Mono<Void> uploadError(String fileHash) {
            return null;
        }

        /**
         * 如果构造传入需要本地合并，则返回文件，否则 FileNotFoundEx -> 父类注释不要是这个
         *
         * @param localFilePath 转储到的本地路径
         * @return void
         */
        @Override
        public Mono<Void> transferTo(String localFilePath) {
            return null;
        }
    }
}
