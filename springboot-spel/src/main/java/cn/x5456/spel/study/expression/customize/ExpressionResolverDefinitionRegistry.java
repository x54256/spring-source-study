package cn.x5456.spel.study.expression.customize;

import java.util.Set;

/**
 * 表达式解析器定义注册中心
 *
 * @author yujx
 * @date 2021/03/19 09:46
 */
public interface ExpressionResolverDefinitionRegistry extends ExpressionResolverAliasRegistry {

    /**
     * 注册表达式解析器定义
     *
     * @param name                         表达式解析器的名称
     * @param expressionResolverDefinition 表达式解析器定义
     */
    void registerExpressionResolverDefinition(String name, ExpressionResolverDefinition expressionResolverDefinition);

    /**
     * 移除表达式解析器定义
     *
     * @param name 表达式解析器的名称
     */
    void removeExpressionResolverDefinition(String name);


    /**
     * 根据名称获取表达式解析器定义
     *
     * @param name 表达式解析器的名称
     */
    ExpressionResolverDefinition getExpressionResolverDefinition(String name);


    /**
     * 是否包含表达式解析器定义
     *
     * @param name 表达式解析器的名称
     * @return 是否包含
     */
    boolean containsExpressionResolverDefinition(String name);

    /**
     * @return 获取所有表达式解析器的名称
     */
    Set<String> getExpressionResolverDefinitionNames();

    /**
     * @return 注册中心的表达式解析器的数量
     */
    int getExpressionResolverDefinitionCount();
}
