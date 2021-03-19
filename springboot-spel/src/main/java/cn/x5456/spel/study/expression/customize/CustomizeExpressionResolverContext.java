package cn.x5456.spel.study.expression.customize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author yujx
 * @date 2021/03/18 09:49
 */
@Component
public class CustomizeExpressionResolverContext {

    // todo 多个 key 对应一个值的 map
    // todo(其实还是要看下别名那个是咋处理的):这个 map 要改一下。不能把 ExpressionResolverDefinition 作为 value
    private static final Map<String, ExpressionResolverDefinition> customizeExpressionMap = new ConcurrentHashMap<>();

    /**
     * 调用 ImportExpressionResolverDefinitionRegistrar#registerExpressionResolverDefinitions() 方法，向当前类注册表达式解析的方法
     */
    @Autowired
    public void init(List<ImportExpressionResolverDefinitionRegistrar> importResolverDefRegistrarList) {
        for (ImportExpressionResolverDefinitionRegistrar importResolverDefRegistrar : importResolverDefRegistrarList) {
            importResolverDefRegistrar.registerExpressionResolverDefinitions(this);
        }
    }

    /**
     * 注册自定义表达式
     */
    public void registerExpressionResolverDefinition(String name, ExpressionResolverDefinition expressionResolverDefinition) {
        // TODO: 2021/3/18 别名
        customizeExpressionMap.put(name, expressionResolverDefinition);
    }

    /**
     * 解析 @{} 表达式
     *
     * @param placeholder @{} 表达式
     * @return 解析后的结果
     */
    public String resolvePlaceholder(String placeholder) {
        String value = placeholder;
        if (customizeExpressionMap.containsKey(placeholder)) {
            ExpressionResolverDefinition expressionResolverDefinition = customizeExpressionMap.get(placeholder);
            Supplier<String> supplier = expressionResolverDefinition.getExpressionResolver();
            value = supplier.get();
        }
        return value;
    }

    public String defaultValue(String placeholder) {
        String value = placeholder;
        if (customizeExpressionMap.containsKey(placeholder)) {
            ExpressionResolverDefinition expressionResolverDefinition = customizeExpressionMap.get(placeholder);
            value = expressionResolverDefinition.getDefaultValue();
        }
        return value;
    }
}
