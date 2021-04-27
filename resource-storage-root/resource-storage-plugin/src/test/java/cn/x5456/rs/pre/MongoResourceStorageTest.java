package cn.x5456.rs.pre;

import cn.hutool.core.util.IdUtil;
import cn.x5456.rs.pre.document.FsResourceInfo;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
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

//    @Test
//    public void doUploadFile() {
//        // 307d0d853c038c3aa60a23b24ce059dbac78df0511ac822face64d1da0dafcec
//        String hash = DigestUtil.sha256Hex(new File("/Users/x5456/Desktop/dus.mdj"));
//        Flux<DataBuffer> bufferFlux = DataBufferUtils.read(new FileSystemResource("/Users/x5456/Desktop/dus.mdj"), new DefaultDataBufferFactory(), 256 * 1024);
//        Mono<FileMetadata> mono = resourceStorage.doUploadFile(hash, bufferFlux);
//        System.out.println(mono.block());
//    }

    @Test
    public void uploadFile() {
        // 307d0d853c038c3aa60a23b24ce059dbac78df0511ac822face64d1da0dafcec
        Mono<FsResourceInfo> mono = resourceStorage.uploadFile("/Users/x5456/Desktop/高可用可伸缩微服务架构（基于dubbo、springcloud和servicemesh）.pdf", IdUtil.simpleUUID());
        System.out.println("mono.block() = " + mono.block());
    }

//    @Test
//    public void download() {
//        Mono<String> mono = resourceStorage.download("ba1791be11a884b8ea4b45776c4b757d6bb0a5bf4c9887d06b81aff201622bc4");
//        System.out.println(mono.block());
//    }

    @Test
    public void downloadFile() {
        System.out.println(resourceStorage.downloadFile("/Users/x5456/Desktop/111.pdf", "dbe05333604e41699aab19072a534991").block());
    }

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

    @Test
    public void downloadFileDataBuffer() {
    }

    @Test
    public void deleteFile() {
    }

    @Test
    public void getFileName() {
    }

    @Test
    public void getBigFileUploader() {
    }
}