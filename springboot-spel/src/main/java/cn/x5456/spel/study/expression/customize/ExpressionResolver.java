package cn.x5456.spel.study.expression.customize;

import java.util.function.Supplier;

/**
 * @author yujx
 * @date 2021/03/18 10:59
 */
@FunctionalInterface
public interface ExpressionResolver extends Supplier<String> {

//    /**
//     * 由于像 330000 这样的会被 SPEL 解析成 int 类型，从而无法使用 String 类型的方法，所以我们需要手动的将其 toString
//     * <p>
//     * 模仿{@link Function#identity()}
//     *
//     * 没有用，向下面这个表达式，他先计算 #{330512.toString()，结果还是会被他们解析成 int 类型，所以如果用到了 String 的方法还是加 toString() 吧
//     * #{#{330512.toString()}.substring(0, 4).concat(00)}
//     */
//    static ExpressionResolver toSPELString(ExpressionResolver resolver) {
//        return () -> "#{" + resolver.get() + ".toString()}";
//    }
}
