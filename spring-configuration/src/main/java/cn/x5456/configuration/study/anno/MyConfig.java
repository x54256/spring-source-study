package cn.x5456.configuration.study.anno;

import cn.x5456.configuration.study.beans.Bean2;
import cn.x5456.configuration.study.beans.imports.ImportBean1;
import cn.x5456.configuration.study.beans.properties.PropertiesBean1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

@Configuration
@Import({ImportBean1.class, MyImportSelector.class, MyImportBeanDefinitionRegistrar.class})
@ComponentScan("cn.x5456.configuration.study.beans.scan")
@ImportResource("classpath:applicationContext.xml")
@PropertySource("classpath:my.properties")
@MyEnableAnnotation
public class MyConfig {

    @Autowired
    Environment environment;

    @Bean
    public PropertiesBean1 getBean1() {
        PropertiesBean1 bean1 = new PropertiesBean1();
        bean1.setAge(environment.getProperty("age", Integer.class));
        bean1.setName(environment.getProperty("name"));
        return bean1;
    }

    // 内部类
    public class InnerClass {

        @Bean
        public Bean2 getBean2() {
            return new Bean2();
        }
    }

    public class InnerClass2 {

    }
}