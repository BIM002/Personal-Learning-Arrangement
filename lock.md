###锁的分类

###### 1.对资源对象是否加锁 __`悲观锁`__ 和 __`乐观锁`__

#####悲观锁

悲观锁是一种悲观思想,它总是认为最坏的情况即会认为数据会被别的线程读取修改
所以悲观锁在使用数据时会对数据对象锁住,这样其他线程想修改该数据就会阻塞,直到数据资源被释放

1.数据库本身的锁功能基本都是悲观锁,如行锁、表锁、读锁、写锁

2.synchronize和reentrantLock等独占锁(排它锁)也是悲观锁思想实现

#####乐观锁

与悲观锁相反,乐观锁认为数据不会被别的线程修改,所以读取时不会加锁,但是在写入时会判断当前数据是否被修改
乐观锁适用于多读场所可以提高程序的吞吐量
1.数据库通过表字段版本号控制

2.CAS[Compare-and-Swap](j.u.c 的atomic包就是使用的乐观锁机制)

###### 2.若资源已锁定,线程已阻塞可分为 __`自旋锁`__

自旋锁的定义：当一个线程尝试去获取某一把锁的时候，
如果这个锁此时已经被别人获取(占用)，那么此线程就无法获取到这把锁，
该线程将会等待，间隔一段时间后会再次尝试获取。这种采用循环加锁 -> 等待的机制被称为自旋锁(spinlock)。

![Image text](https://raw.githubusercontent.com/BIM002/Personal-Learning-Arrangement/master/pic/spinlock.webp)

自旋锁是的线程竞争锁时cpu不需要从内核态到用户态的切换进入阻塞状态，他们只需要自旋等待，直到锁释放后获取，避免了cpu切换的消耗
所以操作系统内核经常使用自旋锁。

但是如果在自旋中一直竞争到锁，将会非常消耗性能，它阻止了其他线程的运行和调度。
线程中持有锁的时间越长，该线程越容易被调度程序中断。

如果发生中断情况，那么其他线程将保持旋转状态(反复尝试获取锁)，而持有该锁的线程并不打算释放锁，
这样导致的是结果是无限期推迟，直到持有锁的线程可以完成并释放它为止。

通过设置自旋时间，等到时间会自动释放自旋锁。JDK在1.6 引入了适应性自旋锁，适应性自旋锁意味着自旋时间不是固定的了，
而是由前一次在同一个锁上的自旋时间以及锁拥有的状态来决定，基本认为一个线程上下文切换的时间是最佳的一个时间。  

```
public class SpinLockTest{
    private AtomicBoolean available = new AtomicBoolean(false);

    public void lock(){
        while(!tryLock()){
           //doSomething
        }
    }

    public boolean tryLock(){
        return available.compareAndSet(false,true);
    }

    public void releaseLock(){
        if(!available.compareAndSet(true,false)){
            throw new RuntimeException("释放锁失败!");
        }
    }
}
```

###### 3.通过Synchronized可以分为 __`无锁`__ 、__`偏向锁`__、__`轻量级锁`__、__`重量级锁`__

synchronized初始为无锁

###### 4.锁的公平性分为 __`公平锁`__ 与 __`非公平锁`__

###### 5.锁是否可重入分为 __`可重入锁`__ 与 __`不可重入锁`__

###### 6.多个线程是否能获取同一把锁 __`独占锁`__ 与 __`共享锁`__



