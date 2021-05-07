package cn.x5456.rs.pre.cleanstrategy.redis.v1;

import cn.hutool.core.bean.BeanUtil;
import cn.x5456.rs.pre.document.FileMetadata;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author yujx
 * @date 2021/04/29 17:01
 */
@Slf4j
@Deprecated
//@Component
public class MongoEventListener extends AbstractMongoEventListener<FileMetadata> implements MessageListener {

   /*
    A）x5456:rs:files:hash           存放真实数据          过期时间 + 5min          hash
    B）x5456:rs:expirations:timeout  桶（存放 C 类型键）    过期时间 + 5min          set
    C）x5456:rs:files:expires:hash      引用                 过期时间             string


    为啥不直接取出 B 类型键里面的 set，循环触发事件，而是通过 C 类型键的过期来触发？


     */

    public static final String DEFAULT_NAMESPACE = "x5456:rs";

    private String namespace = DEFAULT_NAMESPACE + ":";

    private final RedisTemplate<Object, Object> redis;

    public MongoEventListener(RedisTemplate<Object, Object> redis) {
        if (redis.getKeySerializer() instanceof JdkSerializationRedisSerializer) {
            redis.setKeySerializer(new StringRedisSerializer());
        }
        this.redis = redis;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public void onAfterSave(AfterSaveEvent<FileMetadata> event) {
        FileMetadata metadata = event.getSource();
        if (!metadata.getMultipartUpload()) {
            // 获取文件 hash
            String fileHash = metadata.getFileHash();
            // 获取过期时间，先写死 20s 吧
            long expireInSeconds = 60;
            // 过期时间 + 5min
            long fiveMinutesAfterExpires = expireInSeconds + TimeUnit.MINUTES.toSeconds(5);

            // 保存 A 类型键
            String aKey = this.getDataKey(fileHash);
            RedisCacheInfo msg = RedisCacheInfo.builder().isTemp(false)
                    .id(metadata.getId())
                    .fileHash(metadata.getFileHash()).build();
            BoundHashOperations<Object, Object, Object> aKeyOps = redis.boundHashOps(aKey);
            aKeyOps.putAll(BeanUtil.beanToMap(msg));
            aKeyOps.expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);

            // 保存 C 类型键
            String cKey = namespace + "files:expires:" + fileHash;
            BoundValueOperations<Object, Object> cKeyOps = redis.boundValueOps(cKey);
            cKeyOps.append("");
            cKeyOps.expire(expireInSeconds, TimeUnit.SECONDS);

            // 保存 B 类型键
            long originalRoundedUp = this.roundUpToNextMinute(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireInSeconds));
            String bKey = this.getExpirationKey(originalRoundedUp);
            BoundSetOperations<Object, Object> bKeyOps = redis.boundSetOps(bKey);
            bKeyOps.add(cKey);
            bKeyOps.expire(fiveMinutesAfterExpires, TimeUnit.SECONDS);
        }
    }

    @NotNull
    private String getDataKey(String fileHash) {
        return namespace + "files:" + fileHash;
    }

    @NotNull
    private String getExpirationKey(long timeInMs) {
        return namespace + "expirations:" + timeInMs;
    }

    private long roundUpToNextMinute(long timeInMs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.add(Calendar.MINUTE, 1);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }

    void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        long prevMin = this.roundDownMinute(now);

        log.debug("Cleaning up sessions expiring at " + new Date(prevMin));

        String expirationKey = this.getExpirationKey(prevMin);
        Set<Object> sessionsToExpire = this.redis.boundSetOps(expirationKey).members();
        if (sessionsToExpire != null) {
            this.redis.delete(expirationKey);
            for (Object session : sessionsToExpire) {
                String sessionKey = (String) session;
                this.touch(sessionKey);
            }
        }
    }

    private void touch(String key) {
        this.redis.hasKey(key);
    }

    private long roundDownMinute(long timeInMs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }

    /**
     * Callback for processing received objects through Redis.
     *
     * @param message message must not be {@literal null}.
     * @param pattern pattern matching the channel (if specified) - can be {@literal null}.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        String key = (String) redis.getValueSerializer().deserialize(body);
        String prefix = namespace + "files:expires:";
        if (key == null || !key.startsWith(prefix)) {
            return;
        }

        String fileHash = key.substring(prefix.length());
        log.info("fileHash：「{}」", fileHash);

        String dataKey = this.getDataKey(fileHash);
        Map<Object, Object> data = redis.boundHashOps(dataKey).entries();
        RedisCacheInfo redisCacheInfo = BeanUtil.toBean(data, RedisCacheInfo.class);

        if (redisCacheInfo.getIsTemp()) {

        } else {

        }
    }

    @Data
    @Builder
    static class RedisCacheInfo implements Serializable {
        private String id;
        private String fileHash;

        // 只有是 temp 表的数据这个参数为 true，且有 chunk 参数值
        private Boolean isTemp;
        private Integer chunk;
    }
}
