package cn.x5456.spel.study.expression;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.ImmutableList;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 【适配器模式】
 * 解析 #{} 表达式，从配置文件中取值，最后一个解析 #{} 表达式
 *
 * @author yujx
 * @date 2021/03/17 14:33
 */
@Component
public class SPELExpressionResolver extends AbstractExpressionResolver {

    private static final String CUSTOMIZE_EXPRESSION_PREFIX = "#{";

    private static final String CUSTOMIZE_EXPRESSION_SUFFIX = "}";

    private final SpelExpressionParser parser = new SpelExpressionParser();

    private static final List<Class<?>> CAN_TOSTRING_CLASS_LIST = ImmutableList.of(
            Number.class,
            CharSequence.class,
            Boolean.class,
            Character.class
    );

    public SPELExpressionResolver() {
        super.setPlaceholderPrefix(CUSTOMIZE_EXPRESSION_PREFIX);
        super.setPlaceholderSuffix(CUSTOMIZE_EXPRESSION_SUFFIX);
    }

    /**
     * 解析表达式的值
     */
    @Override
    protected String resolvePlaceholder(String placeholder) {
        Object value = parser.parseExpression(placeholder).getValue();
        if (value != null &&
                CAN_TOSTRING_CLASS_LIST.stream().anyMatch(x -> x.isAssignableFrom(value.getClass()))) {
            return value.toString();
        }
        throw new ResolveExpressionException(
                StrUtil.format("表达式的结果必须是 String 类型，当前表达式为{}，结果为：{}", placeholder, value)
        );
    }

    /**
     * @return 返回的值越小优先级越高
     */
    @Override
    public int getOrder() {
        return 2;
    }

}
