package cn.x5456.rs.pre.def;

import cn.hutool.core.lang.Pair;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BigFileUploader {

    /**
     * 是否已经存在（秒传）
     *
     * @param fileHash 文件 hash
     * @return 文件是否已在服务中存在
     */
    Mono<Boolean> secondPass(String fileHash);

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
    Mono<Boolean> uploadFileChunk(String fileHash, int chunk, Flux<DataBuffer> dataBufferFlux);

    /**
     * 获取上传进度 {1: 上传完成， 0: 上传中，2：失败}  1  2  4  8  与运算
     *
     * @param fileHash 文件 hash
     * @return 上传进度
     */
    Flux<Pair<Integer, UploadProgress>> uploadProgress(String fileHash);

    /**
     * 全部上传完成（文件名，总块数"用来做校验"），有什么办法保证数据无法被更改
     *
     * @param fileHash            文件 hash
     * @param fileName            文件名
     * @param totalNumberOfChunks 当前文件一共多少片
     * @param path                服务上存储的标识
     * @return 操作是否成功
     */
    Mono<Boolean> uploadCompleted(String fileHash, String fileName, int totalNumberOfChunks, String path);

    /**
     * 上传失败，清理
     *
     * @param fileHash 文件 hash
     * @return void
     */
    Mono<Void> uploadError(String fileHash);

    /**
     * 如果构造传入需要本地合并，则返回文件，否则 FileNotFoundEx -> 父类注释不要是这个
     *
     * @param localFilePath 转储到的本地路径
     * @return void
     */
    Mono<Void> transferTo(String localFilePath);

}