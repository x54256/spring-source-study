package cn.x5456.autoconfig.study.beans;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @author yujx
 * @date 2021/01/15 09:27
 */
@Component
public class Bean1 implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println(Arrays.toString(applicationContext.getBeanDefinitionNames()));
    }

    /*
    // 这五个是 Spring 自带的，我也不知道是干啥的
    org.springframework.context.annotation.internalConfigurationAnnotationProcessor
    org.springframework.context.annotation.internalAutowiredAnnotationProcessor
    org.springframework.context.annotation.internalCommonAnnotationProcessor
    org.springframework.context.event.internalEventListenerProcessor
    org.springframework.context.event.internalEventListenerFactory

    // 这个使我们的启动类
    appBootstrap

    // 当前 bean
    bean1

    // 一堆自动配置类，和配置类引入的 bean
    org.springframework.boot.autoconfigure.internalCachingMetadataReaderFactory
    org.springframework.boot.autoconfigure.AutoConfigurationPackages
    org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration
    propertySourcesPlaceholderConfigurer
    org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration
    mbeanExporter
    objectNamingStrategy
    mbeanServer
    org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration
    springApplicationAdminRegistrar
    org.springframework.boot.autoconfigure.aop.AopAutoConfiguration$ClassProxyingConfiguration
    org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
    org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration
    org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor
    org.springframework.boot.context.internalConfigurationPropertiesBinderFactory
    org.springframework.boot.context.internalConfigurationPropertiesBinder
    org.springframework.boot.context.properties.ConfigurationPropertiesBeanDefinitionValidator
    org.springframework.boot.context.properties.ConfigurationBeanFactoryMetadata
    org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration
    spring.info-org.springframework.boot.autoconfigure.info.ProjectInfoProperties
    org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
    taskExecutorBuilder
    applicationTaskExecutor
    spring.task.execution-org.springframework.boot.autoconfigure.task.TaskExecutionProperties
    org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration
    taskSchedulerBuilder
    spring.task.scheduling-org.springframework.boot.autoconfigure.task.TaskSchedulingProperties
     */

}
