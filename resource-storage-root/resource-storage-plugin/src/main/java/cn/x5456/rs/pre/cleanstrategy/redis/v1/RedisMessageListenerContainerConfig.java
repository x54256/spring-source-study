package cn.x5456.rs.pre.cleanstrategy.redis.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Deprecated
//@Configuration
public class RedisMessageListenerContainerConfig {

    @Autowired
    private TopicMessageListener messageListener;

//    @Autowired
//    private TaskThreadPoolConfig config;
//
//    @Value("spring.redis.topic")
//    private String topic;

    @Bean // 配置线程池
    public Executor myTaskAsyncPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AsyncThread-");

        // rejection-policy：当pool已经达到max size的时候，如何处理新任务
        // CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

//    @Bean
//    public RedisMessageListenerContainer configRedisMessageListenerContainer(RedisConnectionFactory factory, ConfigureRedisAction configureRedisAction,
//                                                                             @Qualifier("myTaskAsyncPool") Executor executor) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        // 设置Redis的连接工厂
//        container.setConnectionFactory(factory);
//        // 设置监听使用的线程池
//        container.setTaskExecutor(executor);
//        // 设置监听的Topic
//        ChannelTopic channelTopic = new ChannelTopic("__keyevent@0__:expired");
//        // 设置监听器
//        container.addMessageListener(messageListener, channelTopic);
//
//        RedisConnection connection = factory.getConnection();
//        configureRedisAction.configure(connection);
//
//        return container;
//    }


}