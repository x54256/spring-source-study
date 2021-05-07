package cn.x5456.rs.pre.cleanstrategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 通过 redis 通知机制清理未上传完成的文件缓存
 *
 * @author yujx
 * @date 2021/04/30 10:09
 */
@Slf4j
@Component
public class RedisCleanStrategy extends AbstractMongoCleanStrategy {

    @Override
    public void start() {

    }
}
