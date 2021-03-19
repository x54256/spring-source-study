package cn.x5456.spel.study.expression;

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

    @Test
    public void evaluate() {
        String evaluate = resolver.evaluate("#{@{currRegionCode_6}.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        System.out.println("evaluate = " + evaluate);
    }

    @Test
    public void test() {
        System.out.println(spelExpressionResolver.evaluate("#{#{1+1}+1}"));
    }

    @Test
    public void testExpression() {
        System.out.println(resolver.testExpression("#{@{currRegionCode_6}.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx"));
    }

    @Test
    public void testAlias() {
        String evaluate = resolver.evaluate("#{@{curr}.toString().substring(0, 4).concat('00')}${JSYDGZQ:JSYDGZQ}.shx");
        System.out.println("evaluate = " + evaluate);
    }
}