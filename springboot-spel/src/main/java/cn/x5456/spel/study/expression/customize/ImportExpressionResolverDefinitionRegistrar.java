package cn.x5456.spel.study.expression.customize;

/**
 * Registrar：注册员
 *
 * @author yujx
 * @date 2021/03/18 10:20
 */
public interface ImportExpressionResolverDefinitionRegistrar {

    /**
     * 用于子类实现，向 CustomizeExpressionResolverRegistry 注册自定义表达式解析器
     *
     * @param registry 自定义表达式的注册中心
     */
    void registerExpressionResolverDefinitions(ExpressionResolverDefinitionRegistry registry);
}
