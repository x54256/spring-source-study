package cn.x5456.spel.study.expression.customize.annotation;

import cn.x5456.spel.study.expression.constants.ValueConstants;

import java.lang.annotation.*;

/**
 * 注册自定义表达式解析器，需要配合 {@link org.springframework.context.annotation.Configuration} 使用
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Blueberry {

	/**
	 * 表达式解析器的名称
	 */
	String value() default "";

	/**
	 * 表达式解析器的别名
	 */
	String[] alias() default {};

	/**
	 * 表达式解析器的默认值，用于监测表达式是否正确时使用
	 *
	 * 因为""也可能是一个默认值
	 */
	String defaultValue() default ValueConstants.DEFAULT_NONE;
}
