package cn.x5456.spel.study.expression;

import org.springframework.core.Ordered;

/**
 * 继承了 Ordered 接口，强制子类返回顺序，顺序与依赖注入 List<\ExpressionResolver> 的顺序相关
 *
 * @author yujx
 * @date 2021/03/17 14:30
 */
public interface ExpressionResolver<T> extends Ordered {

    /**
     * @param placeholder 表达式
     * @return 替换表达式之后的结果
     */
    T evaluate(String placeholder);


    /**
     * 此为测试时使用
     *
     * @param placeholder 表达式
     * @return 替换表达式之后的结果
     */
    default T testExpression(String placeholder){
        return this.evaluate(placeholder);
    }
}
