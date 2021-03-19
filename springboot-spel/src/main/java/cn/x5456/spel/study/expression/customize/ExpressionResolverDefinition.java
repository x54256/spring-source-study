package cn.x5456.spel.study.expression.customize;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author yujx
 * @date 2021/03/18 09:54
 */
public class ExpressionResolverDefinition {

    // 解析表达式的方法
    private ExpressionResolver expressionResolver;

    // 方法的默认值，用于测试的时候
    private String defaultValue;

    // TODO: 2021/3/18 看看 bd 的别名咋做的
    private List<String> aliasList = new ArrayList<>();

    public ExpressionResolverDefinition() {
    }

    public Supplier<String> getExpressionResolver() {
        return expressionResolver;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getAliasList() {
        return aliasList;
    }

    public void setExpressionResolver(ExpressionResolver expressionResolver) {
        this.expressionResolver = expressionResolver;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void addAlias(String alias) {
        this.aliasList.add(alias);
    }

    public void addAlias(List<String> alias) {
        this.aliasList.addAll(alias);
    }
}
