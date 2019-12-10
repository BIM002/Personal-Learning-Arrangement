package com.learing.collection.map;

import java.util.Map;
import java.util.Objects;

/**
 * @author: 10302
 * @Date: 2019/12/10 11:16
 * @Description:
 **/
public class HashMap<K, V> implements BaseMap {
    /**
     * 默认长度
     */
    private int defaultSize = 16;
    /**
     * 默认扩展因子
     */
    private float defaultAddFactor = 0.75f;
    /**
     * 使用长度
     */
    private double useSize;
    /**
     * entry数组
     */
    private Entry<K,V>[] table;

    /**
     * 下一个扩充map的阈值
     */
    private int threshold;

    private static int maxSize = 1 << 30;

    public HashMap() {
        this(16, 0.75f);
    }

    public HashMap(int defaultSize, float defaultAddFactor) {
        if (defaultSize < 0) {
            throw new IllegalArgumentException("数组下标异常");
        }
        if (defaultSize > maxSize){
            this.defaultSize = maxSize;
        }
            if (defaultAddFactor < 0 || Float.isNaN(defaultAddFactor)) {
                throw new IllegalArgumentException("扩展因子异常");
            }
        this.defaultSize = defaultSize;
        this.defaultAddFactor = defaultAddFactor;
        table = new Entry[defaultSize];
        threshold = tableSizeFor(defaultSize);
    }

    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= maxSize) ? maxSize : n + 1;
    }

    /**
     * 根据key计算hash
     *
     * @param k
     * @return
     */
    private int hash(K k) {
        int hash;
        return (k == null) ? 0 : (hash = k.hashCode()) ^ (hash >>> 16);
    }

    /**
     * 根据key与length计算下标
     *
     * @param k
     * @param length
     * @return
     */
    private int getIndex(K k, int length) {
        //key的hash与长度做取模运算
        int index = hash(k) & (length - 1);
        return index > 0 ? index : -index;
    }

    @Override
    public Object put(Object o, Object o2) {

        return null;
    }

    @Override
    public Object get(Object o) {
        return null;
    }

    /**
     * Entry对象
     *
     * @param <K>
     * @param <V>
     */
    public static class Entry<K, V> implements BaseEntry {
        final int hash;
        final K k;
        V v;
        Entry next;

        public Entry(K k, V v, Entry next) {
            this.hash = hashCode();
            this.k = k;
            this.v = v;
            this.next = next;
        }

        @Override
        public int getHash() {
            return hash;
        }

        @Override
        public K getKey() {
            return k;
        }

        @Override
        public V getValue() {
            return v;
        }

        @Override
        public String toString() {
            return k + "=" + v;
        }

        @Override
        public int hashCode() {
            //根据key的hash值与value的hash值 计算散列值
            return Objects.hashCode(k) ^ Objects.hashCode(v);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof HashMap.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) obj;
                //比较k,v
                if (Objects.equals(k, e.getKey()) &&
                        Objects.equals(v, e.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }

}
