package cn.x5456.configuration.study;

import cn.x5456.configuration.study.anno.MyConfig;
import cn.x5456.configuration.study.beans.Bean2;
import cn.x5456.configuration.study.beans.imports.ImportBean1;
import cn.x5456.configuration.study.beans.imports.ImportSelectorBean1;
import cn.x5456.configuration.study.beans.properties.PropertiesBean1;
import cn.x5456.configuration.study.beans.scan.ScanBean1;
import cn.x5456.configuration.study.beans.xml.XmlBean1;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author yujx
 * @date 2021/01/14 10:02
 */
public class AppBootstrap {

    /*
    1）初步测试结果：
        1。如果 @Import 注解引入的类是 ImportSelector、ImportBeanDefinitionRegistrar 的子类，那么不会将其对象放入容器。
        2。标注 @Configuration 注解的内部类，如果包含 @Bean 标注的方法或类上带有 @Component、@ComponentScan、@Import、@ImportResource 注解才会将其加入容器中。
     */
    public static void main(String[] args) {
        // 准备
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan("cn.x5456.configuration.study.anno");
        context.refresh();

        System.out.println("----------------------------");

        // @Import
        // 普通 bean
        System.out.println("@Import-普通bean = " + context.getBean(ImportBean1.class));
        // ImportSelector 引入的
        System.out.println("@Import-ImportSelectorBean = " + context.getBean(ImportSelectorBean1.class));

        // @ComponentScan
        System.out.println("@ComponentScan = " + context.getBean(ScanBean1.class));

        // @ImportResource
        System.out.println("@ImportResource = " + context.getBean(XmlBean1.class));

        // @PropertySource
        System.out.println("@PropertySource = " + context.getBean(PropertiesBean1.class));

        // @Configuration-内部类
        System.out.println("@Configuration-内部类1 = " + context.getBean(MyConfig.InnerClass.class));
        System.out.println("@Configuration-内部类1-@Bean = " + context.getBean(Bean2.class));
        // System.out.println("@Configuration-内部类2 = " + context.getBean(MergeConfig.InnerClass2.class));

    }
}
