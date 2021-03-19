package cn.x5456.spel.study.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 【适配器模式】
 * 解析 ${} 表达式，从配置文件中取值，第二个解析 ${} 表达式
 *
 * @author yujx
 * @date 2021/03/17 14:33
 */
@Component
public class PropertiesExpressionResolver implements StringExpressionResolver {

    private static final Logger log = LoggerFactory.getLogger(PropertiesExpressionResolver.class);

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    /**
     * @param placeholder 表达式
     * @return 替换表达式之后的结果
     */
    @Override
    public String evaluate(String placeholder) {
        log.info("输入的表达式为：【{}】", placeholder);
        String value = placeholder;
        try {
            value = beanFactory.resolveEmbeddedValue(placeholder);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Could not resolve placeholder")) {
                log.warn("表达式【{}】未解析成功，报错内容为：【{}】，如合理请忽略~", placeholder, e.getMessage());
            } else {
                throw e;
            }
        }
        log.info("解析后的结果为：【{}】", value);
        return value;
    }

    /**
     * @return 返回的值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
