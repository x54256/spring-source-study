package cn.x5456.spel.study.expression;

import cn.x5456.spel.study.expression.customize.CustomizeExpressionResolverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 解析 @{} 表达式，从配置文件中取值，第一个解析 @{} 表达式
 *
 * @author yujx
 * @date 2021/03/18 09:40
 */
@Component
public class CustomizeExpressionResolver extends AbstractExpressionResolver {

    private static final Logger log = LoggerFactory.getLogger(CustomizeExpressionResolver.class);

    private static final String CUSTOMIZE_EXPRESSION_PREFIX = "@{";

    private static final String CUSTOMIZE_EXPRESSION_SUFFIX = "}";

    private static final String CUSTOMIZE_VALUE_SEPARATOR = ":";

    @Autowired
    private CustomizeExpressionResolverContext expressionResolverContext;

    public CustomizeExpressionResolver() {
        super.setPlaceholderPrefix(CUSTOMIZE_EXPRESSION_PREFIX);
        super.setPlaceholderSuffix(CUSTOMIZE_EXPRESSION_SUFFIX);
        super.setValueSeparator(CUSTOMIZE_VALUE_SEPARATOR);
    }

    /**
     * 解析表达式的值
     */
    // TODO: 2021/3/17 在每一个自定义解析@{}的方法中再加一个方法，提供一个默认值，当实施添加表达式的时候要对表达式进行校验
    @Override
    protected String resolvePlaceholder(String placeholder) {
        return expressionResolverContext.resolvePlaceholder(placeholder);
    }

    /**
     * 此为测试时使用
     *
     * @param placeholder 表达式
     * @return 替换表达式之后的结果
     */
    @Override
    public String testExpression(String placeholder) {
        log.info("正在解析「@\\{}」，输入的表达式为：【{}】", placeholder);
        String value = super.parseStringValue(placeholder, expressionResolverContext::defaultValue);
        log.info("「@\\{}」解析完成，解析后的结果为：【{}】", value);
        return value;
    }

    /**
     * @return 返回的值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
