package cn.x5456.spel.study.expression;

import cn.x5456.spel.study.expression.customize.CustomizeExpressionResolverContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 解析 @{} 表达式，从配置文件中取值，第一个解析 @{} 表达式
 * <p>
 * 可以把这个类里关于 @{ 和 } 的解析抽取出来变为一个抽象类
 * <p>
 * 加一个缓存，把已经计算过的表达式放进去
 *
 * @author yujx
 * @date 2021/03/18 09:40
 */
@Component
public class CustomizeExpressionResolver extends AbstractExpressionResolver {

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
     * @return 返回的值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
