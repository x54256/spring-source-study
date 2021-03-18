package cn.x5456.spel.study.expression.util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多键值映射同一个value的多key map
 *
 * @author George (GeorgeWorld@qq.com)
 */
public class MultiKeyUniqueValueMap<K extends MultiKeyMapKey, V> extends ConcurrentHashMap<K, V> {
    /**
     * 用作内部key的递增计数器
     */
    private AtomicInteger trueKeyFactory = new AtomicInteger();

    public V get(Object key) {
        if (key instanceof MultiKeyMapKey) {
            MultiKeyMapKey mkey = (MultiKeyMapKey) key;
            if (mkey.getTrueKey() != null) {//如果真实key存在，则直接按照真实key来取值
                return this.get(key);
            } else {//如果传进来的key不包含真实key，则
                //循环整个map，寻找可能匹配的key
                for (Map.Entry<K, V> entry : this.entrySet()) {
                    if (entry.getKey().aliasKeyEquals(mkey)) {
                        return entry.getValue();
                    }
                }
            }
        }

        return null;
    }


    @Override
    public V put(K key, V value) {
        if (key == null || value == null) {
            return null;
        }

        //根据value，寻找key
        for (Map.Entry<K, V> entry : this.entrySet()) {
            if (entry.getValue() == value || entry.getValue().equals(value)) {
                //当前待插入的值，已经在map中存在了，则尝试合并key
                entry.getKey().bindingKeys(key);
                return entry.getValue();
            }
        }

        //走到这里，说明是真正的新值
        //为key对象创建真正的key
        Integer trueKey = this.trueKeyFactory.incrementAndGet();
        key.setTrueKey(trueKey);

        return super.put(key, value);
    }


    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m == null || m.isEmpty()) {
            return;
        }


        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }


}