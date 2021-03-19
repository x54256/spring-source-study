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

    @Blueberry
    public String addr(ExpressionResolverConfig config) {
        return "张江";
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
