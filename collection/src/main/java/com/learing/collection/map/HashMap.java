package com.learing.collection.map;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author: 10302
 * @Date: 2019/12/10 11:16
 * @Description:
 **/
public class HashMap<K, V> implements BaseMap<K, V>, Serializable {
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
	 * entry数组  也就是桶
	 */
	private Entry<K, V>[] table;

	/**
	 * 下一个扩充map的阈值
	 */
	private int threshold;

	/**
	 * 表示当前map修改数据次数
	 */
	transient int modCount;

	/**
	 * 表示当前map的键值对个数
	 */
	transient int size;

	/**
	 * 链表转成树结构的阈值
	 * 为什么转成红黑树阈值为8? 时间和空间的权衡
	 * 红黑树存储空间是 node 节点的2倍
	 * 1.在hash算法足够好,遵循泊松分布情况下 链表个数达到8的情况为0.00000006 千万分之6 基本是不可能的 也就节省了空间
	 * 2.链表的查询效率在长度 6时为6/2 = 3 红黑树 log(6) = 2.6;长度8时 为8/2=4 红黑树 log(8) = 3,
	 * 只有当hash算法不好,hash冲突激烈,为了查询检索效率牺牲空间存储转换成红黑树
	 * 红黑树退化成链表阈值为6也是同上原因
	 */
	static final int TREEIFY_THRESHOLD = 8;

	private static int maxSize = 1 << 30;

	public HashMap() {
		this(16, 0.75f);
	}

	public HashMap(int defaultSize, float defaultAddFactor) {
		if (defaultSize < 0) {
			throw new IllegalArgumentException("数组下标异常");
		}
		if (defaultSize > maxSize) {
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

	/**
	 * cap = 129 1000 0001
	 * n = 128   1000 0000
	 * n >>> 1 = 0100 0000
	 * n | n>>>1 = 1100 0000
	 * n >>> 2 = 0011 0000
	 * n | n>>>2 = 1111 0000
	 * x 需要看传入cap的位数 以上述为例8位 位运算结束n为 1111 1111  255
	 * 位运算方式把所有非0计算成1,找出最接近2^x的数
	 * 最后n+1返回2^x的数值
	 *
	 * @param cap
	 * @return
	 */
	static int tableSizeFor(int cap) {
		int n = cap - 1;
		n |= n >>> 1;
		n |= n >>> 2;
		n |= n >>> 4;
		n |= n >>> 8;
		n |= n >>> 16;
		return (n < 0) ? 1 : (n >= maxSize) ? maxSize : n + 1;
	}

	public static void main(String[] args) {
//        java.util.HashMap hashMap = new java.util.HashMap();
//        hashMap.put("test123","test1");
	}

	/**
	 * 根据key计算hash
	 *
	 * @param k
	 * @return
	 */
	private int hash(Object k) {
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
		//key的hash与长度做与运算
		int index = hash(k) & (length - 1);
		return index > 0 ? index : -index;
	}

	@Override
	public V put(K key, V value) {
		//map数组
		Entry<K, V>[] tab;
		Entry<K, V> p;
		int n, i;
		//在放入第一个参数时进行真正的初始化长度
		if ((tab = table) == null || (n = tab.length) == 0) {
			//初始化map的数组长度
			n = tab.length;
		}
		//根据初始化完的数组长度和散列值做与操作
		int hash = hash(key);
		//计算出在hashMap中的落点
		p = tab[i = (n - 1) & hash];
		if (p == null) {
			//无冲突直接放置
			tab[i] = new Entry(hash, key, value, null);
		} else {
			Entry<K, V> e;
			K k = p.k;
			//对象的hash值以及key相同 -> 执行修改
			if (p.hash == hash && (k == key || (key != null && key.equals(k)))) {
				e = p;
			} else if (p instanceof TreeNode) {
				//如果是二叉树类型,增加到树结构下 -> 新增数据
				e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
			} else {
				//执行计算
				//遍历落点桶中的链表数据,插入到链表尾部 -> 新增数据
				for (int binCount = 0; ; ++binCount) {
					if ((e = p.next) == null) {
						//设置下一节点为新数据
						p.next = newEntry(hash, key, value, null);
						//查看bin中总数>=7 -1为开始
						// -1 for 1st
						if (binCount >= TREEIFY_THRESHOLD - 1) {
							//bin从链表转成红黑树(二叉树)
							//treeifyBin(tab, hash);
						}
						break;
					}
					//对象的hash值以及key相同 -> 执行修改
					if (e.hash == hash &&
							((k = e.k) == key || (key != null && key.equals(k)))) {
						break;
					}
					//新增对象赋值
					p = e;
				}
			}
			//e 非空表示当前节点为修改操作
			if (e != null) {
				V oldValue = e.v;
//                if (!onlyIfAbsent || oldValue == null) {
				if (oldValue == null) {
					e.v = value;
				}
//                afterNodeAccess(e);
				return oldValue;
			}
		}
		//表示该map触发修改的次数
		++modCount;
		if (++size > threshold)
			//如果添加参数后大于阈值,扩容
			resize();
		//链表插入后操作
		afterNodeInsertion(evict);
		return null;
	}

	Entry<K, V> newEntry(int hash, K key, V value, Entry<K, V> next) {
		return new Entry<>(hash, key, value, next);
	}

	@Override
	public Object get(Object o) {
		return null;
	}

	/**
	 * 红黑树节点
	 *
	 * @param <K>
	 * @param <V>
	 */
	public static final class TreeNode<K, V> extends Entry<K, V> {
		Entry<K, V> before, after; //其他bin桶
		TreeNode<K, V> parent;  // 红黑树连接节点
		TreeNode<K, V> left; //左节点引用
		TreeNode<K, V> right; //右节点引用
		TreeNode<K, V> prev;    // 需要取消链接后删除
		boolean red;           // 红黑树表示红或黑

		public TreeNode(int hash, K k, V v, Entry next) {
			super(hash, k, v, next);
		}

		final TreeNode<K, V> putTreeVal(HashMap<K, V> map, Entry<K, V>[] tab,
		                                int hash, K key, V value) {
			Class<?> kc = null;
			boolean searched = false;
			//是否有父节点数据
			TreeNode<K, V> root = (parent != null) ? root() : this;
			for (TreeNode<K, V> p = root; ; ) {
				int dir, ph;
				K pk;
				//该节点hash>传入hash 入左
				if ((ph = p.hash) > hash)
					dir = -1;
					//该节点hash<传入hash 入右
				else if (ph < hash)
					dir = 1;
					//节点信息的key与入参key一致表示已存在对象
				else if ((pk = p.k) == key || (key != null && key.equals(pk)))
					return p;
				else if ((kc == null &&
						(kc = comparableClassFor(key)) == null) ||
						(dir = compareComparables(kc, key, pk)) == 0) {
					if (!searched) {
						TreeNode<K, V> q, ch;
						searched = true;
						if (((ch = p.left) != null &&
								(q = ch.find(hash, key, kc)) != null) ||
								((ch = p.right) != null &&
										(q = ch.find(hash, key, kc)) != null))
							return q;
					}
					dir = tieBreakOrder(key, pk);
				}

				TreeNode<K, V> xp = p;
				if ((p = (dir <= 0) ? p.left : p.right) == null) {
					Entry<K, V> xpn = xp.next;
					TreeNode<K, V> x = map.newTreeNode(hash, key, value, xpn);
					if (dir <= 0)
						//左子节点
						xp.left = x;
					else
						//右子节点
						xp.right = x;
					xp.next = x;
					x.parent = x.prev = xp;
					if (xpn != null)
						((TreeNode<K, V>) xpn).prev = x;
					moveRootToFront(tab, balanceInsertion(root, x));
					return null;
				}
			}
		}

		final TreeNode<K, V> root() {
			for (TreeNode<K, V> r = this, p; ; ) {
				if ((p = r.parent) == null)
					return r;
				r = p;
			}
		}
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
		//链表节点
		Entry next;

		public Entry(int hash, K k, V v, Entry next) {
			this.hash = hash;
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
