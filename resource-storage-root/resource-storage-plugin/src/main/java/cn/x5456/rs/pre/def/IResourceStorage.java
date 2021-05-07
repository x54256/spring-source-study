package cn.x5456.rs.pre.def;

import cn.hutool.core.lang.Pair;
import cn.x5456.rs.pre.document.FsResourceInfo;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 从服务（mongo/ftp）获取文件资源接口
 *
 * @author yujx
 * @date 2019/03/23 14:36
 */
public interface IResourceStorage {

    // ============================== 异步 api

    /**
     * 上传文件到文件服务
     *
     * @param localFilePath 本地文件路径
     * @param path          服务上存储的标识
     * @return 是否上传成功
     */
    Mono<FsResourceInfo> uploadFile(String localFilePath, String path);

    /**
     * 上传文件到文件服务
     *
     * @param localFilePath 本地文件路径
     * @param fileName      文件名
     * @param path          服务上存储的标识
     * @return 是否上传成功
     */
    Mono<FsResourceInfo> uploadFile(String localFilePath, String fileName, String path);

    /**
     * dataBufferFlux 方式上传文件到文件服务
     *
     * @param dataBufferFlux dataBufferFlux
     * @param fileName       文件名
     * @param path           服务上存储的标识
     * @return 是否上传成功
     */
    Mono<FsResourceInfo> uploadFile(Flux<DataBuffer> dataBufferFlux, String fileName, String path);

    /**
     * 从文件服务下载文件
     *
     * @param localFilePath 本地文件路径
     * @param path          服务上存储的标识
     * @return 是否下载成功
     */
    Mono<Boolean> downloadFile(String localFilePath, String path);

    /**
     * 从文件服务中获取文件  Pair<String, byte[]>
     *
     * @param path 服务上存储的标识
     * @return Pair key-文件名，value-dataBufferFlux
     */
    Mono<Pair<String, Flux<DataBuffer>>> downloadFileDataBuffer(String path);


//     InputStream   -> Channel

    /**
     * 删除文件服务上的文件（引用计数）
     *
     * @param path 服务上存储的标识
     * @return 是否删除成功
     */
    Mono<Boolean> deleteFile(String path);

    /**
     * 通过path获取文件名
     *
     * @param path 服务上存储的标识
     * @return 文件名【包括后缀】
     */
    Mono<String> getFileName(String path);

    // ============================== 分片上传大文件异步 api


    BigFileUploader getBigFileUploader();


//    // TODO: 2021/4/25 你说文件名和一共几片需不需要我们来管理，还有上传的状态，是否上传完成这种的
//    // TODO: 2021/4/25 如果我们管理就需要有一个生命周期对象，到底返不返回呢（返回就会有线程安全问题），还是自己维护
//
//    /**
//     * dataBufferFlux 方式上传文件到文件服务
//     *
//     * @param dataBufferFlux dataBufferFlux
//     * @param path           服务上存储的标识
//     * @param fileHash       文件 hash
//     * @param chunk          第几片
//     * @return 是否上传成功
//     */
//    Mono<Boolean> uploadFileChunk(Flux<DataBuffer> dataBufferFlux, String path,
//                                  String fileHash, int chunk);


    /*
    0. 构造的时候加一个属性，是否需要本地合并该文件
    1. 检查是否可以秒传(hash)
    2. 上传每一片(hash, part, chunk)
    3. 全部上传完成(hash, fileName, totalNumberOfChunks)

    1 可以直接到 3
     */

    // LRU



    // TODO: 2021/4/25 小文件下载的时候用 0 拷贝
}
