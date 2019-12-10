package com.learing.collection.map;

/**
 * @author: 10302
 * @Date: 2019/12/10 11:17
 * @Description:
 **/
public interface BaseMap<K,V> {

    public V put(K k,V v);

    public V get(K k);
}
