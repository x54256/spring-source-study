package cn.x5456.spel.study.expression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 【废弃这种方案】模仿 {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurerComposite}
 *
 * @author yujx
 * @date 2021/03/17 17:03
 */
@Component
public class CompositeStringExpressionResolver {

    private final List<StringExpressionResolver> delegates = new ArrayList<>();

    @Autowired
    public void init(List<StringExpressionResolver> resolvers) {
        if (!CollectionUtils.isEmpty(resolvers)) {
            this.delegates.addAll(resolvers);
        }
    }

    /**
     * @param expression 表达式
     * @param function
     * @return 替换表达式之后的结果
     */
    public ResolveExpressionTrace template(String expression, Function<StringExpressionResolver, String> function) {
        ResolveExpressionTrace trace = new ResolveExpressionTrace(expression);
        String value;
        try {
            for (StringExpressionResolver resolver : delegates) {
                value = function.apply(resolver);
                trace.addProcess(value);
            }
        } catch (Exception e) {
            trace.recordErrorMsg(e);
        }
        return trace;
    }

    // TODO: 2021/3/18 修改返回值
    public ResolveExpressionTrace testExpression(String expression) {
        return this.template(expression, resolver -> resolver.testExpression(expression));
    }

    public ResolveExpressionTrace evaluate(String expression) {
        return this.template(expression, resolver -> resolver.evaluate(expression));
    }
}
