package cn.x5456.spel.study.expression.customize.annotation;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.x5456.spel.study.expression.customize.ExpressionResolverDefinition;
import cn.x5456.spel.study.expression.customize.ExpressionResolverDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class ExpressionResolverDefinitionReaderV2 implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ExpressionResolverDefinitionReaderV2.class);

    @Autowired
    private ExpressionResolverDefinitionRegistry registry;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(Configuration.class);
        for (String name : beanNames) {
            Class<?> clazz = applicationContext.getType(name);
            if (clazz != null) {
                if (Arrays.stream(clazz.getMethods()).anyMatch(method -> method.isAnnotationPresent(Blueberry.class))) {
                    // 处理 @Blueberry 注解
                    this.processBlueberry(applicationContext.getBean(name), clazz);
                }
            }
        }
    }

//    /**
//     * 由于 @Configuration(proxyBeanMethods = true) 默认为 true，导致获取到的对象是 cglib 代理对象，从而无法获取到他身上的 Configuration 注解
//     */
//    @Override
//    public void onApplicationEvent(ContextRefreshedEvent event) {
//        ApplicationContext applicationContext = event.getApplicationContext();
//        String[] beanNames = applicationContext.getBeanDefinitionNames();
//        for (String name : beanNames) {
//            Class<?> clazz = applicationContext.getType(name);
//            if (clazz != null) {
//                Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
//                if (configuration != null &&
//                        Arrays.stream(clazz.getMethods()).anyMatch(method -> method.isAnnotationPresent(Blueberry.class))) {
//
//                    // 处理 @Blueberry 注解
//                    this.processBlueberry(applicationContext.getBean(name), clazz);
//                }
//            }
//        }
//    }

    private void processBlueberry(Object bean, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Blueberry.class)) {
                if (this.check(method)) {
                    Blueberry blueberry = method.getAnnotation(Blueberry.class);
                    // 如果没有填写值，则使用方法名
                    String name = ObjectUtil.isNotEmpty(blueberry.value()) ? blueberry.value() : method.getName();

                    ExpressionResolverDefinition resolverDefinition = new ExpressionResolverDefinition();
                    resolverDefinition.setExpressionResolver(() -> ReflectUtil.invoke(bean, method).toString());
                    resolverDefinition.setDefaultValue(blueberry.defaultValue());
                    resolverDefinition.addAlias(Arrays.asList(blueberry.alias()));

                    // 注册表达式解析器
                    registry.registerExpressionResolverDefinition(name, resolverDefinition);
                } else {
                    log.error("类「{}」上的方法「{}」格式不符合规范，已被忽略！", clazz.getName(), method.getName());
                }
            }
        }
    }

    private boolean check(Method method) {
        return method.getReturnType().equals(String.class) &&
                method.getParameterCount() == 0;
    }

}