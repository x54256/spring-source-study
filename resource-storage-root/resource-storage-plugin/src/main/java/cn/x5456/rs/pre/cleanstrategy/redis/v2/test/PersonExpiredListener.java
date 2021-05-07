package cn.x5456.rs.pre.cleanstrategy.redis.v2.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;

/**
 * @author yujx
 * @date 2021/05/07 09:47
 */
@Slf4j
//@Component
public class PersonExpiredListener implements ApplicationListener<RedisKeyExpiredEvent<?>> {

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(RedisKeyExpiredEvent<?> event) {
        System.out.println(event.getValue());
        System.out.println(new String(event.getSource()));
        log.info("event：「{}」", event);
    }
}
