package cn.x5456.spel.study.expression;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 单元测试的重要性，有的时候写着写着你自己就不知道在写啥了，单元测试可以帮你纠错
 *
 * @author yujx
 * @date 2021/03/18 11:30
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CompositeStringExpressionResolverTest {

    @Autowired
    private CompositeStringExpressionResolver resolver;

    @Autowired
    private SPELExpressionResolver spelExpressionResolver;

    @Autowired
    private PropertiesExpressionResolver propertiesExpressionResolver;

    @Autowired
    private CustomizeExpressionResolver customizeExpressionResolver;

    @Test
    public void evaluate() {
        ResolveExpressionTrace evaluate = resolver.evaluate("#{@{currRegionCode_6}.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        Assert.assertEquals("220500JSYDGZQ.shx", evaluate.recreate());
    }

    @Test
    public void test() {
        Assert.assertEquals("3", spelExpressionResolver.evaluate("#{#{1+1}+1}"));
    }

    @Test
    public void testExpression() {
        ResolveExpressionTrace evaluate = resolver.testExpression("#{'@{currRegionCode}'.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        Assert.assertEquals("330500JSYDGZQ.shx", evaluate.recreate());
    }

    @Test
    public void testAlias() {
        ResolveExpressionTrace evaluate = resolver.evaluate("#{'@{curr}'.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        Assert.assertEquals("220500JSYDGZQ.shx", evaluate.recreate());
    }

    @Test
    public void testBlueberryExpression() {
        Assert.assertEquals("123", resolver.testExpression("@{phoneNum}").recreate());
    }

    @Test
    public void testBlueberryAlias() {
        Assert.assertEquals("456", resolver.evaluate("@{phone}").recreate());
    }

    @Test(expected = ResolveExpressionTraceException.class)
    public void testError() {
        resolver.evaluate("#{@{currRegionCode}.concat('JSYDGZQ.shx')}").recreate();
    }

    @Test
    public void testDefault() {
        Assert.assertEquals("张江", resolver.testExpression("@{addr}").recreate());
    }

    @Test
    public void testProperties() {
        Assert.assertEquals("${ksdjanksajcna}", propertiesExpressionResolver.evaluate("${ksdjanksajcna}"));
    }

    @Test
    public void testCustomize() {
        Assert.assertEquals("@{ksdjanksajcna}", customizeExpressionResolver.evaluate("@{ksdjanksajcna}"));
    }

    @Test(expected = ResolveExpressionTraceException.class)
    public void testErrorTest() {
        resolver.testExpression("@{dogName}").recreate();
    }
}