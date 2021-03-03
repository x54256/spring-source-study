package cn.x5456.order.study.beans;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 注解@Order或者接口Ordered的作用是定义Spring IOC容器中Bean的执行顺序的优先级，而不是定义Bean的加载顺序，
 * Bean的加载顺序不受@Order或Ordered接口的影响；
 *
 * @author yujx
 * @date 2021/02/19 09:12
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class Bean1 {

    public Bean1() {
        System.out.println("bean1 的构造方法！");
    }

    public static void func() {
        System.out.println("调用静态方法！");
    }
}
