package cn.x5456.spel.study.expression.customize;

import java.util.List;

/**
 * 表达式解析器别名注册中心
 *
 * @author yujx
 * @date 2021/03/19 09:42
 */
public interface ExpressionResolverAliasRegistry {

    /**
     * Given a name, register an alias for it.
     *
     * @param name  the canonical name
     * @param alias the alias to be registered
     * @throws IllegalStateException if the alias is already in use
     *                               and may not be overridden
     */
    void registerAlias(String name, String alias);

    /**
     * Remove the specified alias from this registry.
     *
     * @param alias the alias to remove
     * @throws IllegalStateException if no such alias was found
     */
    void removeAlias(String alias);

    /**
     * Determine whether the given name is defined as an alias
     * (as opposed to the name of an actually registered component).
     *
     * @param name the name to check
     * @return whether the given name is an alias
     */
    boolean isAlias(String name);

    /**
     * Return the aliases for the given name, if defined.
     *
     * @param name the name to check for aliases
     * @return the aliases, or an empty array if none
     */
    List<String> getAliases(String name);

    /**
     * 确定原始名称，将别名解析为规范名称。
     *
     * @param name the user-specified name
     * @return the transformed name
     */
    String canonicalName(String name);
}
