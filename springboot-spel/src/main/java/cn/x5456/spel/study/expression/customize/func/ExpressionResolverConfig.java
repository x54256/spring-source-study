package cn.x5456.spel.study.expression.customize.func;

import cn.x5456.spel.study.expression.customize.annotation.Blueberry;
import org.springframework.context.annotation.Configuration;

/**
 * 方式二：通过 @Configuration + @Blueberry 注册
 *
 * @author yujx
 * @date 2021/03/19 10:26
 */
@Configuration
public class ExpressionResolverConfig {

    @Blueberry(defaultValue = "123", alias = "phone")
    public String phoneNum() {
        return "456";
    }

    /*
     由于没有设置表达式的名称，则默认名称为方法名 addr。
     因为没有配置默认值，当调用 CustomizeExpressionResolver.testExpression 方法时会调用这个方法返回"张江"
     */
    @Blueberry
    public String addr(ExpressionResolverConfig config) {
        return "张江";
    }

    /*
     由于没有设置表达式的名称，则默认名称为方法名 addr。
     因为没有配置默认值，当调用 CustomizeExpressionResolver.testExpression 方法时会调用这个方法并且报错
     */
    @Blueberry
    public String dogName() {
        // 此处模拟的是从 ThreadLocal 中取出参数，但是却没有时的报错
        int a = 1 / 0;
        return "哒哒";
    }

    // 错误示范，返回值类型不正确
    @Blueberry
    public void func() {
    }

    // 错误示范 2，方法参数从 Spring 容器中获取不到
    @Blueberry
    public String func2(String s) {
        return "func2";
    }
}
