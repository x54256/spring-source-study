package cn.x5456.rs.pre.def;

import cn.hutool.core.io.FileUtil;
import cn.x5456.rs.pre.MongoResourceStorage;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CountDownLatch;

/**
 * @author yujx
 * @date 2021/04/28 09:10
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class BigFileUploaderTest {

    private BigFileUploader bigFileUploader;

    @Autowired
    private DataBufferFactory dataBufferFactory;

    @Autowired
    public void setBigFileUploader(MongoResourceStorage mongoResourceStorage) {
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

}