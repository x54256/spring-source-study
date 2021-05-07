package cn.x5456.rs.pre;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.x5456.rs.pre.document.FileMetadata;
import cn.x5456.rs.pre.document.FsResourceInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yujx
 * @date 2021/04/29 09:16
 */
@Slf4j
public class MongoResourceStorageTest extends BaseMongoTest {

    private final String absoluteFileName = "test.txt";
    String path = IdUtil.simpleUUID();

    @Test
    public void uploadFile() {
        // 理论上不会触发这种情况的，因为这个方法第一个执行
        this.deleteIfExist();

        String localFilePath = FileUtil.getAbsolutePath(absoluteFileName);
        FsResourceInfo fsResourceInfo = mongoResourceStorage.uploadFile(localFilePath, path).block();
        log.info("fsResourceInfo：「{}」", fsResourceInfo);

        Query query = Query.query(Criteria.where(FsResourceInfo.PATH).is(fsResourceInfo.getId()));
        Assert.assertEquals(fsResourceInfo, mongoTemplate.findOne(query, FsResourceInfo.class).block());
        try {
            TimeUnit.MINUTES.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteIfExist() {
        Query query = Query.query(Criteria.where(FsResourceInfo.PATH).is(path));
        FsResourceInfo resourceInfo = mongoTemplate.findOne(query, FsResourceInfo.class).block();
        if (resourceInfo != null) {
            this.deleteFile();
        }
    }

    @Test
    public void downloadFile() {
        this.checkFileIsUpload();

        // 随机生成一个本地保存的路径
        String localFilePath = FileUtil.getAbsolutePath(IdUtil.simpleUUID() + ".txt");
        log.info("localFilePath：「{}」", localFilePath);

        Boolean block = mongoResourceStorage.downloadFile(localFilePath, path).block();
        Assert.assertTrue(block);
    }

    private void checkFileIsUpload() {
        Query query = Query.query(Criteria.where(FsResourceInfo.PATH).is(path));
        FsResourceInfo resourceInfo = mongoTemplate.findOne(query, FsResourceInfo.class).block();
        if (resourceInfo == null) {
            this.uploadFile();
        }

        mongoResourceStorage.cleanLocalTemp();
    }

    @Test
    public void downloadFileDataBuffer() {
        this.checkFileIsUpload();

        Pair<String, Flux<DataBuffer>> pair = mongoResourceStorage.downloadFileDataBuffer(path).block();
        Assert.assertEquals(absoluteFileName, pair.getKey());
        Assert.assertNotNull(absoluteFileName, pair.getValue().blockLast());
    }

    @Test
    public void deleteFile() {
        this.checkFileIsUpload();

        Assert.assertTrue(mongoResourceStorage.deleteFile(path).block());
        Query query = Query.query(Criteria.where(FsResourceInfo.PATH).is(path));
        FsResourceInfo resourceInfo = mongoTemplate.findOne(query, FsResourceInfo.class).block();
        Assert.assertNull(resourceInfo);
    }

    @Test
    public void getFileName() {
        this.checkFileIsUpload();

        Assert.assertEquals(absoluteFileName, mongoResourceStorage.getFileName(path).block());
    }


    // ========= 大文件

    String chunk1Name = "chunk1.tmp";
    String chunk2Name = "chunk2.tmp";

    String mergeChunkName = "mergechunk.tmp";
    String hash = SecureUtil.sha256(new File(FileUtil.getAbsolutePath(mergeChunkName)));

    String pathBig = IdUtil.simpleUUID();

    @Test
    public void uploadFileChunk() {
        this.delete();

        String chunk1Path = FileUtil.getAbsolutePath(chunk1Name);
        FileSystemResource chunk1Resource = new FileSystemResource(chunk1Path);
        Boolean chunk1Result = bigFileUploader.uploadFileChunk(hash, 0, DataBufferUtils.read(
                chunk1Resource, dataBufferFactory, MongoResourceStorage.DEFAULT_CHUNK_SIZE)).block();
        Assert.assertTrue(chunk1Result);

        String chunk2Path = FileUtil.getAbsolutePath(chunk2Name);
        FileSystemResource chunk2Resource = new FileSystemResource(chunk2Path);
        Boolean chunk2Result = bigFileUploader.uploadFileChunk(hash, 1, DataBufferUtils.read(
                chunk2Resource, dataBufferFactory, MongoResourceStorage.DEFAULT_CHUNK_SIZE)).block();
        Assert.assertTrue(chunk2Result);
    }

    private void delete() {
        mongoResourceStorage.deleteFile(pathBig).block();
        this.uploadError();
    }

    // 同时上传两个相同的
    @Test
    public void uploadFileChunkSame() throws InterruptedException {
        String chunk1Path = FileUtil.getAbsolutePath(chunk1Name);
        FileSystemResource chunk1Resource = new FileSystemResource(chunk1Path);

        CountDownLatch latch = new CountDownLatch(2);

        bigFileUploader.uploadFileChunk(hash, 0, DataBufferUtils.read(
                chunk1Resource, dataBufferFactory, MongoResourceStorage.DEFAULT_CHUNK_SIZE))
                .doOnTerminate(latch::countDown)
                .subscribe(x -> log.info("x：「{}」", x));

        bigFileUploader.uploadFileChunk(hash, 0, DataBufferUtils.read(
                chunk1Resource, dataBufferFactory, MongoResourceStorage.DEFAULT_CHUNK_SIZE))
                .doOnTerminate(latch::countDown)
                .subscribe(x -> log.info("x：「{}」", x));

        latch.await();
    }

    @Test
    public void uploadProgress() {

    }

    @Test
    public void uploadCompleted() {
        this.uploadFileChunk();

        Boolean block = bigFileUploader.uploadCompleted(hash, mergeChunkName, 2, pathBig).block();
        Assert.assertTrue(block);
    }

    @Test
    public void secondPass() {
        this.uploadCompleted();

        Assert.assertTrue(bigFileUploader.isExist(hash).block());
    }

    @Test
    public void uploadError() {
        Assert.assertTrue(bigFileUploader.uploadError(hash).block());
    }


    @Test
    public void test() {
        FileMetadata metadata = new FileMetadata();
        metadata.setFileHash("123");
        metadata.setMultipartUpload(false);
        mongoTemplate.insert(metadata).block();


    }
}