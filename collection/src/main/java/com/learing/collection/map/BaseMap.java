package com.learing.collection.map;

/**
 * @author: 10302
 * @Date: 2019/12/10 11:17
 * @Description:
 **/
public interface BaseMap<K,V> {

    /**
     *
     * @author yelongfei
     * @date 2020/1/8 14:04
     * @param key
     * @param value
     * @return
     * @throws
     */
    V put(K key,V value);

    V get(K k);
}
