package cn.x5456.rs.pre.def;

import cn.x5456.rs.base.EnumInterface;

// TODO: 2021/4/27 上传中断问题
// TODO: 2021/4/27 看看能不能用状态机
public enum UploadProgress implements EnumInterface {

    UPLOADING(0, "上传中"),
    UPLOAD_COMPLETED(1, "上传完成"),
//    UPLOAD_FAILED(2, "上传失败"),
    ;

    int code;
    String desc;

    UploadProgress(int code, String desc) {
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