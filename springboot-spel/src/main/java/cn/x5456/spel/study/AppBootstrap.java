package cn.x5456.spel.study;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author yujx
 * @date 2021/01/15 09:26
 */
@SpringBootApplication
public class AppBootstrap {

//    @Value("#{'${list}'.split(',')}")
//    private List<String> list;
//
//    /*
//    @{}     自定义的表达式
//    ${}     从配置文件中取
//    #{}     spel
//     */
//
//    // 123topic1,topic2,topic3啦啦啦
//    @Value("${name}${list}啦啦啦${yujx")
//    private String abc;

    public static void main(String[] args) {
        new SpringApplication(AppBootstrap.class).run(args);
    }
}
