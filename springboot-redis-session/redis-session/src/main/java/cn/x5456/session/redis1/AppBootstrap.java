package cn.x5456.session.redis1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

/**
 * @author yujx
 * @date 2021/01/15 09:26
 */
/*
加上这个注解用的是 RedisHttpSessionConfiguration 配置类，那么配置的 spring.session.redis 的属性就不会生效
不加的时候用的则是 SessionAutoConfiguration import 的 RedisSessionConfiguration

原因是 SessionAutoConfiguration 中有一个内部配置类 SpringBootRedisHttpSessionConfiguration 继承了 RedisHttpSessionConfiguration，
而且其中的 customize 方法对用户配置的 spring.session.redis 属性进行了定制操作。

为啥加上这个注解，SessionAutoConfiguration import 的 RedisSessionConfiguration 就不生效了？

一个是因为 RedisSessionConfiguration 上有 @ConditionalOnMissingBean(SessionRepository.class) 注解，
另一个就是 SessionAutoConfiguration 是由 DeferredImportSelector（自动装配） 引入进来的，当解析他的时候
@EnableRedisHttpSession 已经生效，容器中已经有了 SessionRepository 对象了。

但是如果想
 */
//@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60)
@SpringBootApplication
public class AppBootstrap {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(AppBootstrap.class, args);
    }

}
