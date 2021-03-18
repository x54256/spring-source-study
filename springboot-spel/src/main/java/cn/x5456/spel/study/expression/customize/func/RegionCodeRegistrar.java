package cn.x5456.spel.study.expression.customize.func;

import cn.x5456.spel.study.expression.customize.CustomizeExpressionResolverContext;
import cn.x5456.spel.study.expression.customize.ExpressionResolverDefinition;
import cn.x5456.spel.study.expression.customize.ImportExpressionResolverDefinitionRegistrar;
import org.springframework.stereotype.Component;

/**
 * @author yujx
 * @date 2021/03/18 10:32
 */
@Component
public class RegionCodeRegistrar implements ImportExpressionResolverDefinitionRegistrar {

    /**
     * 用于子类实现，向 CustomizeExpressionResolverRegistry 注册自定义表达式解析器
     *
     * @param context 自定义表达式上下文（注册中心）
     */
    @Override
    public void registerExpressionResolverDefinitions(CustomizeExpressionResolverContext context) {
        // 注册 计算当前行政区划的规则
        ExpressionResolverDefinition currRegionCodeResolver = new ExpressionResolverDefinition();
        currRegionCodeResolver.setExpressionResolver(this::currRegionCode);
        currRegionCodeResolver.setDefaultValue("330512000000");
        context.registerExpressionResolverDefinition("currRegionCode", currRegionCodeResolver);

        // 注册 计算当前行政区划（6 位）的规则
        ExpressionResolverDefinition currRegionCode6Resolver = new ExpressionResolverDefinition();
        currRegionCode6Resolver.setExpressionResolver(this::currRegionCode_6);
        currRegionCode6Resolver.setDefaultValue("330512");
        context.registerExpressionResolverDefinition("currRegionCode_6", currRegionCode6Resolver);

        // 注册 计算上位行政区划的规则
        ExpressionResolverDefinition upperRegionCodeResolver = new ExpressionResolverDefinition();
        upperRegionCodeResolver.setExpressionResolver(this::upperRegionCode);
        upperRegionCodeResolver.setDefaultValue("330500000000");
        context.registerExpressionResolverDefinition("upperRegionCode", upperRegionCodeResolver);

        // 注册 计算上位行政区划（6 位）的规则
        ExpressionResolverDefinition upperRegionCode6Resolver = new ExpressionResolverDefinition();
        upperRegionCode6Resolver.setExpressionResolver(this::upperRegionCode_6);
        upperRegionCode6Resolver.setDefaultValue("330500");
        context.registerExpressionResolverDefinition("upperRegionCode_6", upperRegionCode6Resolver);
    }

    public String currRegionCode() {
        return "330512000000";
    }

    public String currRegionCode_6() {
        return currRegionCode().substring(0, 6);
    }

    public String upperRegionCode() {
        return currRegionCode().substring(0, 4).concat("00000000");
    }

    public String upperRegionCode_6() {
        return upperRegionCode().substring(0, 6);
    }
}
