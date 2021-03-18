package cn.x5456.spel.study.expression.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 多key映射map的默认key对象
 *
 * @author George (GeorgeWorld@qq.com)
 */
public class MultiKeyMapKey {
    /**
     * 用在map中作为真实key，不能由应用层来赋值，必须在map内部赋值
     */
    private Integer trueKey;
    /**
     * 别名key列表
     */
    private List<Object> aliasKeyList = new ArrayList<Object>();


    final Integer getTrueKey() {
        return trueKey;
    }

    final void setTrueKey(Integer trueKey) {
        this.trueKey = trueKey;
    }

    public void addKey(Object key) {
        aliasKeyList.add(key);
    }

    public List<Object> getAliasKeyList() {
        return aliasKeyList;
    }

    public void bindingKeys(MultiKeyMapKey newKey) {
        if (newKey == null || newKey.getAliasKeyList().isEmpty()) {
            return;
        }

        //合并两个key的别名key列表
        List<Object> newKeyList = new ArrayList<Object>();
        for (Object key : aliasKeyList) {
            for (Object nkey : newKey.getAliasKeyList()) {
                if (!(key == nkey || (key.getClass() == nkey.getClass()) && (key.equals(nkey)))) {
                    newKeyList.add(nkey);
                }
            }
        }

        if (!newKeyList.isEmpty()) {
            aliasKeyList.addAll(newKeyList);
        }
    }

    public boolean aliasKeyEquals(MultiKeyMapKey newKey) {
        if (this == newKey) {
            return true;
        }
        if (this.trueKey == newKey.getTrueKey()) {
            return true;
        }

        for (Object key : aliasKeyList) {
            for (Object nkey : newKey.getAliasKeyList()) {
                if ((key.getClass() == nkey.getClass()) && key.equals(nkey)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiKeyMapKey that = (MultiKeyMapKey) o;
        return Objects.equals(trueKey, that.trueKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trueKey);
    }
}