package cn.x5456.rs.pre.cleanstrategy.redis.v2.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.index.Indexed;

@Data
@AllArgsConstructor
//@RedisHash(value = "persons", timeToLive = 20)
public class Person {
    @Id
    private String id;

    @Indexed
    private String name;
}