package cn.x5456.rs.pre;

import cn.hutool.core.util.IdUtil;
import cn.x5456.rs.pre.def.BigFileUploader;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.scheduler.Scheduler;

import javax.annotation.PostConstruct;

/**
 * @author yujx
 * @date 2021/04/29 09:42
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class BaseMongoTest {

    @Autowired
    DataBufferFactory dataBufferFactory;

    @Autowired
    MongoConverter converter;

    ReactiveMongoTemplate mongoTemplate;

    MongoResourceStorage mongoResourceStorage;

    BigFileUploader bigFileUploader;

    String databaseName = IdUtil.simpleUUID();

    @PostConstruct
    public void preClass() {
        MongoClient mongoClient = MongoClients.create();
        SimpleReactiveMongoDatabaseFactory factory = new SimpleReactiveMongoDatabaseFactory(mongoClient, databaseName);

        ReactiveMongoTemplate reactiveMongoTemplate = new ReactiveMongoTemplate(factory);
        ReactiveGridFsTemplate reactiveGridFsTemplate = new ReactiveGridFsTemplate(factory, converter);

        this.mongoTemplate = reactiveMongoTemplate;
        this.mongoResourceStorage = new MongoResourceStorage(dataBufferFactory, reactiveMongoTemplate,
                reactiveGridFsTemplate, new ObjectProvider<Scheduler>() {
            @Override
            public Scheduler getObject(Object... args) throws BeansException {
                return null;
            }

            @Override
            public Scheduler getIfAvailable() throws BeansException {
                return null;
            }

            @Override
            public Scheduler getIfUnique() throws BeansException {
                return null;
            }

            @Override
            public Scheduler getObject() throws BeansException {
                return null;
            }
        });
        this.bigFileUploader = mongoResourceStorage.getBigFileUploader();

        log.info("创建的数据库名为：「{}」", databaseName);
    }
}
