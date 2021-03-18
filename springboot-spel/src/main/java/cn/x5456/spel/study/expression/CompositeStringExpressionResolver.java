package cn.x5456.spel.study.expression;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
     * @return 替换表达式之后的结果
     */
    public String evaluate(String expression) {
        String value = expression;
        for (StringExpressionResolver resolver : delegates) {
            value = resolver.evaluate(value);
        }
        return value;
    }
}
