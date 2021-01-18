package cn.x5456.autoconfig.study;

import cn.x5456.autoconfig.study.customize.StarterController;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author yujx
 * @date 2021/01/15 09:26
 */
@SpringBootApplication
public class AppBootstrap implements ApplicationContextAware {
    public static void main(String[] args) {
        SpringApplication.run(AppBootstrap.class, args);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContext.getBean(StarterController.class).hello();
    }
}
