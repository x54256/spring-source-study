package cn.x5456.rs.pre;

import cn.hutool.core.util.IdUtil;
import cn.x5456.rs.pre.document.FsResourceInfo;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Enumeration;

/**
 * @author yujx
 * @date 2021/04/26 10:44
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class MongoResourceStorageTest {

    @Autowired
    private MongoResourceStorage resourceStorage;

    @Test
    public void uploadFile() {
        Mono<FsResourceInfo> mono = resourceStorage.uploadFile("/Users/x5456/Desktop/111.pdf", IdUtil.objectId());
        Assert.assertNotNull(mono.block());
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