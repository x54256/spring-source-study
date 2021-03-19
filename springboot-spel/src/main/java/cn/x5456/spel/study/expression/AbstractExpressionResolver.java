package cn.x5456.spel.study.expression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 解析表达式的抽象父类
 *
 * @author yujx
 * @date 2021/03/17 14:33
 */
public abstract class AbstractExpressionResolver implements StringExpressionResolver {

    private static final Logger log = LoggerFactory.getLogger(AbstractExpressionResolver.class);

    // 表达式前缀
    private String placeholderPrefix;

    // 表达式后缀
    private String placeholderSuffix;

    // 值分隔符
    private String valueSeparator = null;

    // 已知的简单前缀
    private static final Map<String, String> wellKnownSimplePrefixes = new HashMap<>(4);

    // 简单前缀
    private String simplePrefix;

    static {
        wellKnownSimplePrefixes.put("}", "{");
        wellKnownSimplePrefixes.put("]", "[");
        wellKnownSimplePrefixes.put(")", "(");
    }

    /**
     * @param placeholder 表达式
     * @return 替换表达式之后的结果
     */
    @Override
    public String evaluate(String placeholder) {
        log.info("正在解析「{}{}」，输入的表达式为：【{}】", placeholderPrefix, placeholderSuffix, placeholder);
        String value = this.parseStringValue(placeholder, this::resolvePlaceholder);
        log.info("「{}{}」解析完成，解析后的结果为：【{}】", placeholderPrefix, placeholderSuffix, value);
        return value;
    }

    /**
     * 解析占位符
     * <p>
     * copy {@link PropertyPlaceholderHelper#parseStringValue}
     */
    protected String parseStringValue(String expression, Function<String, String> resolvePlaceholderFunction) {
        int startIndex = expression.indexOf(this.placeholderPrefix);
        if (startIndex == -1) {
            return expression;
        }

        StringBuilder result = new StringBuilder(expression);
        while (startIndex != -1) {
            int endIndex = findPlaceholderEndIndex(result, startIndex);
            if (endIndex == -1) {
                break;
            }

            // 截取出占位符的值，例如 @{abc} -> abc
            String placeholder = result.substring(startIndex + this.placeholderPrefix.length(), endIndex);

            // 递归调用，解析占位符键中包含的占位符。例如：@{@{xxx}}
            placeholder = parseStringValue(placeholder, resolvePlaceholderFunction);
            // 解析表达式，获取值
            String propVal = resolvePlaceholderFunction.apply(placeholder);
            if (propVal == null && valueSeparator != null) {
                // 如果表达式是 @{abc:123} 这种格式的则获取 @{abc} 的值，否则取默认值 123
                int separatorIndex = placeholder.indexOf(this.valueSeparator);
                if (separatorIndex != -1) {
                    String actualPlaceholder = placeholder.substring(0, separatorIndex);
                    String defaultValue = placeholder.substring(separatorIndex + this.valueSeparator.length());
                    propVal = resolvePlaceholderFunction.apply(actualPlaceholder);
                    if (propVal == null) {
                        propVal = defaultValue;
                    }
                }
            }
            if (propVal != null) {
                // 递归调用，解析先前解析的占位符值中包含的占位符。例如 @{abc} 解析占位符之后变成 @{123}
                propVal = parseStringValue(propVal, resolvePlaceholderFunction);
                result.replace(startIndex, endIndex + this.placeholderSuffix.length(), propVal);
                log.trace("已解析完成的占位符【{}】，值为【{}】", placeholder, propVal);

                startIndex = result.indexOf(this.placeholderPrefix, startIndex + propVal.length());
            } else {
                // 否则，忽略无法解析的占位符 【为啥刚刚测试会报错呢，因为${}解析没有将 ignoreUnresolvablePlaceholders 设置为 true】
                startIndex = result.indexOf(this.placeholderPrefix, endIndex + this.placeholderSuffix.length());
            }
        }

        return result.toString();
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + this.placeholderPrefix.length();
        // 表示嵌套了几层占位符
        int withinNestedPlaceholder = 0;
        while (index < buf.length()) {
            // 如果遇到了闭合符号 } ，并且没有嵌套，则返回当前索引，否则加上闭合符号的长度并把 withinNestedPlaceholder - 1
            if (StringUtils.substringMatch(buf, index, this.placeholderSuffix)) {
                if (withinNestedPlaceholder > 0) {
                    withinNestedPlaceholder--;
                    index = index + this.placeholderSuffix.length();
                } else {
                    return index;
                }
            // 如果遇到了 { 则表示表达式嵌套，withinNestedPlaceholder++
            } else if (StringUtils.substringMatch(buf, index, this.simplePrefix)) {
                withinNestedPlaceholder++;
                index = index + this.simplePrefix.length();
            } else {
                index++;
            }
        }
        return -1;
    }

    /**
     * 解析表达式的值
     *
     * @return 如果没有解析出来，请返回 null
     */
    protected abstract String resolvePlaceholder(String placeholder);

    public void setPlaceholderPrefix(String placeholderPrefix) {
        this.placeholderPrefix = placeholderPrefix;
    }

    public void setPlaceholderSuffix(String placeholderSuffix) {
        this.placeholderSuffix = placeholderSuffix;
        if (wellKnownSimplePrefixes.containsKey(placeholderSuffix)) {
            this.simplePrefix = wellKnownSimplePrefixes.get(placeholderSuffix);
        }
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }
}
