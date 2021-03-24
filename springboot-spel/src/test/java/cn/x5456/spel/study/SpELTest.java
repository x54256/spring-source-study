package cn.x5456.spel.study;

import org.junit.Test;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yujx
 * @date 2021/03/22 15:29
 */
public class SpELTest {

    ExpressionParser parser = new SpelExpressionParser();

    @Test
    public void testFilterData() {

        // ====== 数据准备
        Map<Long, Object> map1 = new HashMap<Long, Object>() {{
            put(1L, "是");
            put(2L, 0);
        }};
        Map<Long, Object> map2 = new HashMap<Long, Object>() {{
            put(1L, "否");
            put(2L, 12);
        }};
        Map<Long, Object> map3 = new HashMap<Long, Object>() {{
            put(1L, "是");
            put(2L, 100);
        }};
        List<Map<Long, Object>> list = Arrays.asList(map1, map2, map3);

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("list", list);

        // ====== 过滤数据

        // [{1=是, 2=0}, {1=是, 2=100}]
        List<Map<Long, Object>> value = (List<Map<Long, Object>>) parser.parseExpression("#list.?[#this[1] eq '是']").getValue(ctx, List.class);
        System.out.println("value = " + value);

        // [{1=否, 2=12}, {1=是, 2=100}]
        List<Map<Long, Object>> value2 = (List<Map<Long, Object>>) parser.parseExpression("#list.?[#this[2] > 10]").getValue(ctx, List.class);
        System.out.println("value = " + value2);
    }

    @Test
    public void test() {
        // ====== 数据准备
        Map<Long, Object> map1 = new HashMap<Long, Object>() {{
            put(1L, "是");
            put(2L, 0);
        }};
        Map<Long, Object> map2 = new HashMap<Long, Object>() {{
            put(1L, "否");
            put(2L, 12);
        }};
        Map<Long, Object> map3 = new HashMap<Long, Object>() {{
            put(1L, "是");
            put(2L, 100);
        }};
        List<Map<Long, Object>> list = Arrays.asList(map1, map2, map3);

        StandardEvaluationContext ctx = new StandardEvaluationContext();
        ctx.setVariable("list", list);

        // ====== 统计数据
        // a. count
        System.out.println(parser.parseExpression("#list.size()").getValue(ctx, Long.class));

//        int sum = list.stream().map(x -> x.get(2L)).mapToInt(x -> (Integer) x).sum();
        // b. sum
        System.out.println(parser.parseExpression("T(cn.x5456.spel.study.SpELTest).sum(#list.![2])").getValue(ctx, Long.class));

//        CollUtil

    }

    public static Integer sum(List<Integer> list) {
        return list.stream().mapToInt(x -> (Integer) x).sum();
    }

}
