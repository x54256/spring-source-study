package cn.x5456.spel.study.expression;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
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
        Assert.assertEquals(evaluate.recreate(), "220500JSYDGZQ.shx");
    }

    @Test
    public void test() {
        Assert.assertEquals(spelExpressionResolver.evaluate("#{#{1+1}+1}"), "3");
    }

    @Test
    public void testExpression() {
        ResolveExpressionTrace evaluate = resolver.testExpression("#{'@{currRegionCode}'.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        Assert.assertEquals(evaluate.recreate(), "330500JSYDGZQ.shx");
    }

    @Test
    public void testAlias() {
        ResolveExpressionTrace evaluate = resolver.evaluate("#{'@{curr}'.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        Assert.assertEquals(evaluate.recreate(), "220500JSYDGZQ.shx");
    }

    @Test
    public void testBlueberryExpression() {
        Assert.assertEquals(resolver.testExpression("@{phoneNum}").recreate(), "123");
    }

    @Test
    public void testBlueberryAlias() {
        Assert.assertEquals(resolver.evaluate("@{phone}").recreate(), "456");
    }

    @Test(expected = RuntimeException.class)
    public void testError() {
        resolver.evaluate("#{@{currRegionCode}.concat('JSYDGZQ.shx')}").recreate();
    }

    @Test
    public void testProperties() {
        Assert.assertEquals(propertiesExpressionResolver.evaluate("${ksdjanksajcna}"), "${ksdjanksajcna}");
    }

    @Test
    public void testCustomize() {
        Assert.assertEquals(customizeExpressionResolver.evaluate("@{ksdjanksajcna}"), "@{ksdjanksajcna}");
    }
}