package cn.x5456.autoconfig.study.customize;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(ControllerProperties.class)
public class StarterAutoConfiguration {

    @Bean
    public StarterController starterController(ControllerProperties controllerProperties) {
        return new StarterController(controllerProperties.getMsg());
    }
}