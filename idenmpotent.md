#幂等性
##接口:
***保证多个支付订单请求唯一性***

解决方式:

1.token

2.悲观锁 select * from xxx for update

3.乐观锁 version字段

4.状态机

5.分布式锁

6.通过商户号以及支付序列码


mq: