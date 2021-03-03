package cn.x5456.redis.study;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author yujx
 * @date 2021/01/15 09:26
 */
@SpringBootApplication
public class AppBootstrap {
    public static void main(String[] args) {
        new SpringApplication(AppBootstrap.class).run(args);
    }

    @Autowired
    private StringRedisTemplate template;

    public void func() {
//        template.execute(new RedisCallback<Object>() {
//            @Override
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                return connection.execute();
//            }
//        })
    }


}
