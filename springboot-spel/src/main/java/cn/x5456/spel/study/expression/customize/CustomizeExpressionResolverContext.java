package cn.x5456.spel.study.expression.customize;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author yujx
 * @date 2021/03/18 09:49
 */
@Component
public class CustomizeExpressionResolverContext implements ExpressionResolverDefinitionRegistry, ExpressionResolverContext {

    /**
     * 表达式解析器名称与表达式解析器定义的映射关系
     */
    private final Map<String, ExpressionResolverDefinition> expressionResolverDefinitionMap = new ConcurrentHashMap<>(16);

    /**
     * Map from alias to canonical name.
     */
    private final Map<String, String> aliasMap = new ConcurrentHashMap<>(16);

    /**
     * 调用 ImportExpressionResolverDefinitionRegistrar#registerExpressionResolverDefinitions() 方法，向当前类注册表达式解析的方法
     */
    @Autowired
    public void init(List<ImportExpressionResolverDefinitionRegistrar> importResolverDefRegistrarList) {
        for (ImportExpressionResolverDefinitionRegistrar importResolverDefRegistrar : importResolverDefRegistrarList) {
            importResolverDefRegistrar.registerExpressionResolverDefinitions(this);
        }
    }

    /**
     * 注册自定义表达式
     */
    @Override
    public void registerExpressionResolverDefinition(String name, ExpressionResolverDefinition expressionResolverDefinition) {
        expressionResolverDefinitionMap.put(name, expressionResolverDefinition);
        for (String alias : expressionResolverDefinition.getAliasList()) {
            if (alias.equals(name)) {
                continue;
            }
            aliasMap.put(alias, name);
        }
    }

    /**
     * 移除表达式解析器定义
     *
     * @param name 表达式解析器的名称
     */
    @Override
    public void removeExpressionResolverDefinition(String name) {
        expressionResolverDefinitionMap.remove(name);
    }

    /**
     * 根据名称获取表达式解析器定义
     *
     * @param name 表达式解析器的名称
     */
    @Override
    public ExpressionResolverDefinition getExpressionResolverDefinition(String name) {
        return expressionResolverDefinitionMap.get(name);
    }

    /**
     * 是否包含表达式解析器定义
     *
     * @param name 表达式解析器的名称
     * @return 是否包含
     */
    @Override
    public boolean containsExpressionResolverDefinition(String name) {
        return expressionResolverDefinitionMap.containsKey(name);
    }

    /**
     * @return 获取所有表达式解析器的名称
     */
    @Override
    public Set<String> getExpressionResolverDefinitionNames() {
        return expressionResolverDefinitionMap.keySet();
    }

    /**
     * @return 注册中心的表达式解析器的数量
     */
    @Override
    public int getBeanDefinitionCount() {
        return expressionResolverDefinitionMap.size();
    }


    /**
     * Given a name, register an alias for it.
     *
     * @param name  the canonical name
     * @param alias the alias to be registered
     * @throws IllegalStateException if the alias is already in use
     *                               and may not be overridden
     */
    @Override
    public void registerAlias(String name, String alias) {
        if (name.equals(alias)) {
            return;
        }
        aliasMap.put(alias, name);
    }

    /**
     * Remove the specified alias from this registry.
     *
     * @param alias the alias to remove
     * @throws IllegalStateException if no such alias was found
     */
    @Override
    public void removeAlias(String alias) {
        aliasMap.remove(alias);
    }

    /**
     * Determine whether the given name is defined as an alias
     * (as opposed to the name of an actually registered component).
     *
     * @param name the name to check
     * @return whether the given name is an alias
     */
    @Override
    public boolean isAlias(String name) {
        return aliasMap.containsKey(name);
    }

    /**
     * Return the aliases for the given name, if defined.
     *
     * @param name the name to check for aliases
     * @return the aliases, or an empty array if none
     */
    @Override
    public List<String> getAliases(String name) {
        List<String> result = new ArrayList<>();
        synchronized (this.aliasMap) {
            retrieveAliases(name, result);
        }
        return result;
    }

    /**
     * 可传递地检索给定名称的所有别名。（检索别名的别名）
     *
     * @param name   the target name to find aliases for
     * @param result the resulting aliases list
     */
    private void retrieveAliases(String name, List<String> result) {
        this.aliasMap.forEach((alias, registeredName) -> {
            if (registeredName.equals(name)) {
                result.add(alias);
                retrieveAliases(alias, result);
            }
        });
    }

    /**
     * 确定原始名称，将别名解析为规范名称。
     *
     * @param name the user-specified name
     * @return the transformed name
     */
    public String canonicalName(String name) {
        String canonicalName = name;
        // Handle aliasing...
        String resolvedName;
        do {
            resolvedName = this.aliasMap.get(canonicalName);
            if (resolvedName != null) {
                canonicalName = resolvedName;
            }
        }
        while (resolvedName != null);
        return canonicalName;
    }

    /**
     * 解析 @{} 表达式
     *
     * @param placeholder @{} 表达式
     * @return 解析后的结果
     */
    @Override
    public String resolvePlaceholder(String placeholder) {
        String value = placeholder;
        // 查找出他的真实名称
        String nameToLookup = this.canonicalName(placeholder);
        if (expressionResolverDefinitionMap.containsKey(nameToLookup)) {
            ExpressionResolverDefinition expressionResolverDefinition = expressionResolverDefinitionMap.get(nameToLookup);
            Supplier<String> supplier = expressionResolverDefinition.getExpressionResolver();
            value = supplier.get();
        }
        return value;
    }

    /**
     * 检测表达式是否正确时使用，返回注册的默认值
     *
     * @param placeholder @{} 表达式
     * @return 解析后的结果
     */
    @Override
    public String defaultValue(String placeholder) {
        String value = placeholder;
        // 查找出他的真实名称
        String nameToLookup = this.canonicalName(placeholder);
        if (expressionResolverDefinitionMap.containsKey(nameToLookup)) {
            ExpressionResolverDefinition expressionResolverDefinition = expressionResolverDefinitionMap.get(nameToLookup);
            value = expressionResolverDefinition.getDefaultValue();
        }
        return value;
    }
}
