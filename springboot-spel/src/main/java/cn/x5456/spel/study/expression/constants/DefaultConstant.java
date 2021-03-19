package cn.x5456.spel.study.expression.constants;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @author yujx
 * @date 2021/03/19 17:16
 */
public final class DefaultConstant {

    public static final List<Class<?>> CAN_TOSTRING_CLASS_LIST = ImmutableList.of(
            Number.class,
            CharSequence.class,
            Boolean.class,
            Character.class
    );

    private DefaultConstant() {
    }
}
