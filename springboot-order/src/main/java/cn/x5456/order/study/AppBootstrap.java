package cn.x5456.order.study;

import cn.x5456.order.study.beans.Bean1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

/**
 * @author yujx
 * @date 2021/01/15 09:26
 */
@SpringBootApplication
public class AppBootstrap {
    public static void main(String[] args) {
        SpringApplication.run(AppBootstrap.class, args);
    }

    public AppBootstrap() {
        System.out.println("主启动类初始化！");
    }

    @PostConstruct
    public void init() {
        Bean1.func();
    }
}
