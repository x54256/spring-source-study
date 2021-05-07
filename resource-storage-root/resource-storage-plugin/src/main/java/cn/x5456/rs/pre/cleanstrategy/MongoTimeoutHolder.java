package cn.x5456.rs.pre.cleanstrategy;

import cn.hutool.core.util.ReflectUtil;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author yujx
 * @date 2021/04/30 10:27
 */
@Component
public final class MongoTimeoutHolder {

    /**
     * 超过连接超时时间多少倍进行删除
     */
    private static final double cardinalNumber = 5;

    private static int connectTimeoutMS;

    @Autowired
    public void init(ReactiveMongoTemplate mongoTemplate) {
        ReactiveMongoDatabaseFactory mongoDatabaseFactory =
                (ReactiveMongoDatabaseFactory) ReflectUtil.getFieldValue(mongoTemplate, "mongoDatabaseFactory");
        MongoClient mongo = (MongoClient) ReflectUtil.getFieldValue(mongoDatabaseFactory, "mongo");
        MongoClientSettings settings = mongo.getSettings();
        SocketSettings socketSettings = settings.getSocketSettings();
        // 默认是 10s
        connectTimeoutMS = socketSettings.getConnectTimeout(TimeUnit.MILLISECONDS);
    }

    public static long getConnectTimeout(TimeUnit timeUnit) {
        return timeUnit.convert(connectTimeoutMS, MILLISECONDS);
    }

    public static long getConnectTimeoutMS() {
        return connectTimeoutMS;
    }

    public static long getCleanTimeoutMS() {
        return (long) (connectTimeoutMS * cardinalNumber);
    }

    public static long getCleanTimeout(TimeUnit timeUnit) {
        return timeUnit.convert(getCleanTimeoutMS(), MILLISECONDS);
    }
}
