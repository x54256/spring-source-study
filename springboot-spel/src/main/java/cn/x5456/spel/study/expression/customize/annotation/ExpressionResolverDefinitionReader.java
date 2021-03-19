package cn.x5456.spel.study.expression.customize.annotation;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.x5456.spel.study.expression.customize.CustomizeExpressionResolverContext;
import cn.x5456.spel.study.expression.customize.ExpressionResolverDefinition;
import cn.x5456.spel.study.expression.customize.ExpressionResolverDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

/**
 * 由于 DI List 的特殊性（只会把容器中有的注入进去，不会查找类型获取他们），所以如果在这个时机获取到 ExpressionResolverDefinitionRegistry
 * 会导致 {@link CustomizeExpressionResolverContext#init(java.util.List)} 不会进行注入。故废弃该类
 *
 * @deprecated {@link ExpressionResolverDefinitionReaderV2}
 *
 * @author yujx
 * @date 2021/03/19 10:28
 */
//@Component
@Deprecated
public class ExpressionResolverDefinitionReader implements BeanFactoryPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(ExpressionResolverDefinitionReader.class);

    private volatile ExpressionResolverDefinitionRegistry registry;

    // TODO: 2021/3/19 理论上获取到配置类之后还要排序下的，我就不做了
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        Iterator<String> beanNamesIterator = beanFactory.getBeanNamesIterator();
        while (beanNamesIterator.hasNext()) {
            String name = beanNamesIterator.next();
            Class<?> clazz = beanFactory.getType(name);
            if (clazz != null) {
                Configuration configuration = AnnotationUtil.getAnnotation(clazz, Configuration.class);
                if (configuration != null &&
                        Arrays.stream(clazz.getMethods()).anyMatch(method -> method.isAnnotationPresent(Blueberry.class))) {
                    // 初始化 ExpressionResolverDefinitionRegistry
                    this.initRegistry(beanFactory);

                    // 处理 @Blueberry 注解
                    this.processBlueberry(beanFactory.getBean(name), clazz);
                }
            }
        }
    }

    /**
     * 为什么不能 DI 进来？
     * <p>
     * ![](https://tva1.sinaimg.cn/large/008eGmZEly1gop1pgvj81j32280tmtnl.jpg)
     */
    private void initRegistry(ConfigurableListableBeanFactory beanFactory) {
        if (registry == null) {
            synchronized (this) {
                if (registry == null) {
                    registry = beanFactory.getBean(ExpressionResolverDefinitionRegistry.class);
                }
            }
        }
    }

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
