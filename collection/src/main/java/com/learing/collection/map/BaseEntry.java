package com.learing.collection.map;

/**
 * @author: 10302
 * @Date: 2019/12/10 11:16
 * @Description:
 **/
public interface BaseEntry<K,V> {

    public int getHash();

    public K getKey();

    public V getValue();
}
