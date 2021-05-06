package cn.x5456.rs.pre.cleanstrategy;

import cn.hutool.core.util.ReflectUtil;
import cn.x5456.rs.pre.def.CleanUnUploadedTempStrategy;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.connection.SocketSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author yujx
 * @date 2021/04/30 10:27
 */
public abstract class AbstractMongoCleanStrategy implements CleanUnUploadedTempStrategy {

    /**
     * 超过连接超时时间多少倍进行删除
     */
    private final double cardinalNumber = 2;

    private int connectTimeoutMS;

    protected ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public void init(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;

        ReactiveMongoDatabaseFactory mongoDatabaseFactory =
                (ReactiveMongoDatabaseFactory) ReflectUtil.getFieldValue(mongoTemplate, "mongoDatabaseFactory");
        MongoClient mongo = (MongoClient) ReflectUtil.getFieldValue(mongoDatabaseFactory, "mongo");
        MongoClientSettings settings = mongo.getSettings();
        SocketSettings socketSettings = settings.getSocketSettings();
        // 默认是 10s
        this.connectTimeoutMS = socketSettings.getConnectTimeout(TimeUnit.MILLISECONDS);
    }

    public long getConnectTimeout(TimeUnit timeUnit) {
        return timeUnit.convert(connectTimeoutMS, MILLISECONDS);
    }

    public long getConnectTimeoutMS() {
        return connectTimeoutMS;
    }

    public long getCleanTimeoutMS() {
        return (long) (connectTimeoutMS * cardinalNumber);
    }

    public long getCleanTimeout(TimeUnit timeUnit) {
        return timeUnit.convert(this.getCleanTimeoutMS(), MILLISECONDS);
    }
}
