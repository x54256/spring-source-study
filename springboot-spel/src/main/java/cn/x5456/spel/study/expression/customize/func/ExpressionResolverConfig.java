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

    @Blueberry(defaultValue = "17521340109", alias = "phone")
    public String phoneNum() {
        return "15842500563";
    }

    // 错误示范
    @Blueberry
    public void func() {
    }
}
