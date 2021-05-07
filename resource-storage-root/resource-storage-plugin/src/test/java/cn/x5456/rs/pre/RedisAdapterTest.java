package cn.x5456.rs.pre;

import cn.x5456.rs.pre.cleanstrategy.redis.v2.RedisCacheInfo;
import cn.x5456.rs.pre.cleanstrategy.redis.v2.RedisCacheInfoRepo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * @author yujx
 * @date 2021/05/07 09:24
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisAdapterTest {

//    @Autowired
//    private RedisTemplate<Object, Object> redisTemplate;
//
//    @Autowired
//    private PersonRepository repository;
//
//    @Test
//    public void testPerson() {
////        RedisKeyValueAdapter redisKeyValueAdapter = new RedisKeyValueAdapter(redisTemplate);
////        // 启用 key expiry listener
////        redisKeyValueAdapter.setEnableKeyspaceEvents(RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP);
////
////        redisKeyValueAdapter.
//
//        Person person = new Person("1", "张三");
////        Person person2 = new Person("2", "李四");
////        Person person3 = new Person("3", "李四");
//        repository.save(person);
////        repository.save(person2);
////        repository.save(person3);
//    }

    @Autowired
    private RedisCacheInfoRepo redisCacheInfoRepo;

    @Test
    public void test() {
        RedisCacheInfo build = RedisCacheInfo.builder()
                .id("123")
                .fileHash("123")
                .isTemp(false)
                .ttl(20L)
                .expirationOfNextMinute(roundUpToNextMinute(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20)))
                .build();

        redisCacheInfoRepo.save(build);
    }

    private long roundUpToNextMinute(long timeInMs) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timeInMs);
        date.add(Calendar.MINUTE, 1);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
        return date.getTimeInMillis();
    }

}
