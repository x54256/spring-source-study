package cn.x5456.rs.pre.cleanstrategy.redis.v2;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RedisCacheInfo implements Serializable {
    private String id;
    private String fileHash;

    // 只有是 temp 表的数据这个参数为 true，且有 chunk 参数值
    private Boolean isTemp;
    private Integer chunk;
}