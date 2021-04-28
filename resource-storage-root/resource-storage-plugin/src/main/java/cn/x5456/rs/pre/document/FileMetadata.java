package cn.x5456.rs.pre.document;

import cn.x5456.rs.base.EnumInterface;
import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * @author yujx
 * @date 2021/04/25 16:56
 */
@Data
@Document("fs.metadata")
public class FileMetadata {

    public static final String FILE_HASH = "fileHash";

    @Id
    private String id;

    // TODO: 2021/4/25 上传的过程中，有人上传多个怎么办，完了，涉及到分布式锁了
    // 文件 hash 值，建议使用 sha256
    @Indexed(unique = true)
    private String fileHash;

    // 推测文件类型，如果没有推测出则设置为 null（不建议使用）
    @Deprecated
    private String fileType;

    // 文件的引用计数，仅供参考 todo cas 更新 https://blog.csdn.net/chinatopno1/article/details/108916905
    private Integer referenceCount;

    // 当前文件对应 fs.files 表中的多少行记录
    private Integer totalNumberOfChunks;

    // 当前文件与 fs.files 表的关联信息；使用临时表解决线程安全问题  {@link cn.x5456.rs.pre.document.FsFileTemp}
    private List<FsFilesInfo> fsFilesInfoList;

    // 其他元数据信息
    private Map<String, Object> metadata;

    // TODO: 2021/4/26 要不要加状态呢
    private Status status;

    @Data
    @AllArgsConstructor
    public static class FsFilesInfo {

        // 第几片
        @NonNull    // 不知道好不好使
        private Integer chunk;

        // 对应的 fs.files 表的 id
        private String fsFilesId;

        // 当前片的大小
        private Long chunkSize;
    }


    // TODO: 2021/4/27 失败
    public enum Status implements EnumInterface {

        初始状态(0, "初始状态"),
        文件上传成功(1, "文件上传成功"),
        文件上传失败(2, "")
//        已就绪(2, "资源已就绪"),
        ;

        int code;
        String desc;

        Status(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        @Override
        public int code() {
            return code;
        }

        @Override
        public String desc() {
            return desc;
        }
    }
}
