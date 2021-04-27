package cn.x5456.rs.pre.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GridFsConfiguration {

    @Bean
    public GridFSBucket gridFSBucket(MongoClient mongoClient) {
        // 先用默认的吧
        return GridFSBuckets.create(mongoClient.getDatabase("test"));
    }

}