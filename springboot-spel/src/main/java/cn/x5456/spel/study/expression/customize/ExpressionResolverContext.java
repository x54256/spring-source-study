package cn.x5456.spel.study.expression.customize;

/**
 * @author yujx
 * @date 2021/03/19 09:53
 */
public interface ExpressionResolverContext {

    /**
     * 解析 @{} 表达式
     *
     * @param placeholder @{} 表达式
     * @return 解析后的结果
     */
    String resolvePlaceholder(String placeholder);

    /**
     * 检测表达式是否正确时使用，返回注册的默认值
     *
     * @param placeholder @{} 表达式
     * @return 解析后的结果
     */
    String defaultValue(String placeholder);
}
