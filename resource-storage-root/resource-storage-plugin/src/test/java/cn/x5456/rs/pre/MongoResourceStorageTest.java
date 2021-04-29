package cn.x5456.rs.pre;

import cn.hutool.core.util.IdUtil;
import cn.x5456.rs.pre.document.FsResourceInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Enumeration;

/**
 * @author yujx
 * @date 2021/04/26 10:44
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class MongoResourceStorageTest {

    @Autowired
    private ReactiveGridFsTemplate gridFsTemplate;

    @Autowired
    private MongoResourceStorage resourceStorage;

    @Test
    public void uploadFile() {
        Mono<FsResourceInfo> mono = resourceStorage.uploadFile("/Users/x5456/Desktop/111.pdf", "6087966cee926fc3c5243a31");
        Assert.assertNotNull(mono.block());
    }

//    @Test
//    public void test() throws FileNotFoundException {
//        try {
//            RandomAccessFile randomAccessFile = new RandomAccessFile("/Users/x5456/Desktop/222.pdf", "rw");
//
//            gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("6088c9f2b33af53cea06363a")))
//                    .flatMap(gridFsTemplate::getResource)
//                    .log()
//                    .map(ReactiveGridFsResource::getDownloadStream)
//                    .flatMap(dataBufferFlux -> {
//                        System.out.println(dataBufferFlux);
//                        return DataBufferUtils.write(dataBufferFlux.log(), randomAccessFile.getChannel());
//                    })
//                    .block();
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//
////        Flux<DataBuffer> read = DataBufferUtils.read(new FileSystemResource("/Users/x5456/Desktop/111.pdf"), new DefaultDataBufferFactory(), MongoResourceStorage.DEFAULT_CHUNK_SIZE);
////        Flux<DataBuffer> write = DataBufferUtils.write(read, new RandomAccessFile("/Users/x5456/Desktop/222.pdf", "rw").getChannel());
////        write.blockLast();
//    }

    @Test
    public void test() throws FileNotFoundException {
//        // 从 mongo 中查找该文件
//        Mono<GridFSFile> gridFSFileMono = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("6088d59655a24b3ffe7b5bd9")));
//        gridFSFileMono.subscribe(gridFSFile -> {
//            Mono<ReactiveGridFsResource> gridFsResourceMono = gridFsTemplate.getResource(gridFSFile);
//            gridFsResourceMono.map(ReactiveGridFsResource::getDownloadStream)   // Mono<Flux<DataBuffer>>
//                    .subscribe(dataBufferFlux -> {
//                        try {
//                            log.info("写入中。。。");
//                            FileChannel channel = new RandomAccessFile("/Users/x5456/Desktop/3.pdf", "rw").getChannel();
////                            AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("/Users/x5456/Desktop/3.pdf"),
////                                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
//                            // DataBufferUtils.write 方法对这个 Flux 进行了消费，又返回了一个新的 Publisher
//                            DataBufferUtils.write(dataBufferFlux, channel)
//                                    .log()
//                                    .doOnComplete(() -> {
//                                        log.info("文件写入完毕...");
//                                        // 不关闭的话如果再上传同一个文件，会报错：java.nio.file.AccessDeniedException，因为资源被占用，无法删除
//                                        log.info("文件流关闭...");
//                                        try {
//                                            channel.close();
//                                        } catch (IOException e) {
//                                            e.printStackTrace();
//                                            log.info("文件流关闭失败...");
//                                        }
//                                    })
//                                    .subscribe();
//
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    });
//
//        });

        FileChannel channel = new RandomAccessFile("/Users/x5456/Desktop/3.pdf", "rw").getChannel();
        gridFsTemplate.findOne(Query.query(Criteria.where("_id").is("6088d59655a24b3ffe7b5bd9")))
                .flatMap(gridFSFile -> gridFsTemplate.getResource(gridFSFile))
                .map(ReactiveGridFsResource::getDownloadStream)
                .flux()
                .flatMap(dataBufferFlux -> {
                    log.info("写入中。。。");
                    return DataBufferUtils.write(dataBufferFlux, channel)
                            .log()
                            .doOnComplete(() -> {
                                log.info("文件写入完毕...");
                                // 不关闭的话如果再上传同一个文件，会报错：java.nio.file.AccessDeniedException，因为资源被占用，无法删除
                                log.info("文件流关闭...");
                                try {
                                    channel.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    log.info("文件流关闭失败...");
                                }
                            });
                }).subscribe();
//                .flatMap(dataBufferFlux -> {
////                    try {
//                        log.info("写入中。。。");
////                        FileChannel channel = new RandomAccessFile("/Users/x5456/Desktop/3.pdf", "rw").getChannel();
////                            AsynchronousFileChannel channel = AsynchronousFileChannel.open(Paths.get("/Users/x5456/Desktop/3.pdf"),
////                                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
//                        // DataBufferUtils.write 方法对这个 Flux 进行了消费，又返回了一个新的 Publisher
//                        return DataBufferUtils.write(dataBufferFlux, channel)
//                                .log()
//                                .doOnComplete(() -> {
//                                    log.info("文件写入完毕...");
//                                    // 不关闭的话如果再上传同一个文件，会报错：java.nio.file.AccessDeniedException，因为资源被占用，无法删除
//                                    log.info("文件流关闭...");
//                                    try {
//                                        channel.close();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                        log.info("文件流关闭失败...");
//                                    }
//                                });
//
//
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
//                }).subscribe();
        ;


        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void downloadFile() {
        Mono<Boolean> mono = resourceStorage.downloadFile("/Users/x5456/Desktop/" + IdUtil.objectId() + ".pdf", "6087966cee926fc3c5243a31");
        Assert.assertTrue(mono.block());
    }

    @Test
    public void downloadFileNotExist() {
        Mono<Boolean> mono = resourceStorage.downloadFile("/Users/x5456/Desktop/" + IdUtil.objectId() + ".pdf", "123");
    }

//    /*
//    同时的时候不行。
//
//    手动改就行。
//
//    不知道，反正分布式的情况下是有用的，先留着吧
//     */
//    @Test
//    public void downloadFileStatusChange() {
//        Mono<FsResourceInfo> monoU = resourceStorage.uploadFile("/Users/x5456/Desktop/111.pdf", "6087966cee926fc3c5243a31");
//        monoU.subscribe();
//
//        Mono<Boolean> monoD = resourceStorage.downloadFile("/Users/x5456/Desktop/222.pdf", "6087966cee926fc3c5243a31");
//        System.out.println(monoD.block());
//    }


    @Test
    public void downloadFileDataBuffer() {
    }

    @Test
    public void deleteFile() {
    }

    @Test
    public void getFileName() {
    }

    // ========== other

    /*
    docx 有一个 word 目录
    xlsx 有一个 xl 目录
    pptx 有一个 ppt 目录
     */
    @Test
    public void t() throws IOException {
        Enumeration<ZipArchiveEntry> entries = new ZipFile("/Users/x5456/Desktop/别人的东西/成果审查与管理系统分享/国土空间规划一张图实施监督信息系统系列培训应用产品--规划成果审查与管理（20200219）-zhanghj+hezy.pptx").getEntries();
        System.out.println(entries);
    }
}