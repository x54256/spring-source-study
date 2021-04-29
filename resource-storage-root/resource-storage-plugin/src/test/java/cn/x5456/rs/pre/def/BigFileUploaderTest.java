package cn.x5456.rs.pre.def;

import cn.hutool.core.io.FileUtil;
import cn.x5456.rs.pre.MongoResourceStorage;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author yujx
 * @date 2021/04/28 09:10
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BigFileUploaderTest {

    private BigFileUploader bigFileUploader;

    private MongoResourceStorage mongoResourceStorage;

    @Autowired
    private DataBufferFactory dataBufferFactory;

    @Autowired
    public void setBigFileUploader(MongoResourceStorage mongoResourceStorage) {
        this.mongoResourceStorage = mongoResourceStorage;
        this.bigFileUploader = mongoResourceStorage.getBigFileUploader();
    }

    @Test
    public void secondPass() {
        Mono<Boolean> secondPass = bigFileUploader.secondPass("123");
        Assert.assertFalse(secondPass.block());
    }

    // 文件 hash，随便编一个了
    String hash = "chunk-hash";


    @Test
    public void uploadFileChunk() {

        // 上传 chunk1
        String chunk1Path = FileUtil.getAbsolutePath("chunk1.tmp");
        Flux<DataBuffer> chunk1DataBuffer = DataBufferUtils.read(new FileSystemResource(chunk1Path), dataBufferFactory, MongoResourceStorage.DEFAULT_CHUNK_SIZE);
        Assert.assertTrue(bigFileUploader.uploadFileChunk(hash, 0, chunk1DataBuffer).block());
    }

    /**
     * 测试同时上传 chunk2
     *
     * 看它用的是一个线程向 mongo 发的请求 ntLoopGroup-2-3（单线程管理多个channel）
     */
    @Test
    public void uploadFileChunkMulti() throws InterruptedException {

        // 上传 chunk2
        String chunk2Path = FileUtil.getAbsolutePath("chunk2.tmp");
        Flux<DataBuffer> chunk1DataBuffer = DataBufferUtils.read(new FileSystemResource(chunk2Path), dataBufferFactory, MongoResourceStorage.DEFAULT_CHUNK_SIZE);

        CountDownLatch latch = new CountDownLatch(2);
        bigFileUploader.uploadFileChunk(hash, 1, chunk1DataBuffer).log()
                .doOnTerminate(latch::countDown)
                .subscribe();
        bigFileUploader.uploadFileChunk(hash, 1, chunk1DataBuffer).log()
                .doOnTerminate(latch::countDown)
                .subscribe();

        latch.await();
    }


    @Test
    public void uploadProgress() {
        bigFileUploader.uploadProgress(hash).log().blockLast();
    }

    @Test
    public void uploadCompleted() {
        Boolean block = bigFileUploader.uploadCompleted(hash, "测试文件合并.txt", 2, "test-path")
                .block();
        Assert.assertTrue(block);
    }

    @Test
    public void uploadError() {
        System.out.println(bigFileUploader.uploadError(hash).block());
    }

    @Test
    public void downloadFile() {
        Mono<Boolean> mono = mongoResourceStorage.downloadFile(FileUtil.getAbsolutePath("测试文件合并.txt"), "test-path");
        Assert.assertTrue(mono.block());
        try {
            TimeUnit.MINUTES.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private ReactiveGridFsTemplate gridFsTemplate;

    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("6088c9f2b33af53cea06363a")))
                .log().subscribe(x -> latch.countDown());
        gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("6088c9fc6dbda711a9b32fab")))
                .log().subscribe(x -> latch.countDown());

        latch.await();
    }

    @Test
    public void testFor() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        for (String s : Arrays.asList("6088c9f2b33af53cea06363a", "6088c9fc6dbda711a9b32fab")) {
            gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(s)))
                    .log().subscribe(x -> latch.countDown());
        }
        latch.await();
    }

//    @Test
//    public void test222() {
//        System.out.println(mongoResourceStorage.doDownload(null, "132").block());
//    }
//
//
//    // 问题根源
//    @Test
//    public void test2223() {
//        System.out.println(mongoResourceStorage.download( "ba1791be11a884b8ea4b45776c4b757d6bb0a5bf4c9887d06b81aff201622bc4").block());
//    }
//
//    // 问题根源
//    @Test
//    public void test555() {
//        mongoResourceStorage.getFileMetadata("ba1791be11a884b8ea4b45776c4b757d6bb0a5bf4c9887d06b81aff201622bc4")
//                .subscribe(m -> {
//
//                    // 这样就好了
//                    new Thread(() -> {
//                        mongoResourceStorage.doDownload(m, "123").block();
//                        log.info("m：「{}」", m);
//                    }).start();
//                });
//
//        try {
//            TimeUnit.HOURS.sleep(3);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}