package com.learing.collection.intercept;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * aop 切面 主要是为了实现业务与公共模块的分离
 * 实现解耦 只需要专注业务逻辑
 * aspectJProxyFactory通过ProxyCreatorSupport来获取通过cglib还是jdk方式代理
 * @author: 10302
 * @Date: 2019/12/13 14:17
 * @Description:
 **/
@Aspect
@Component
@Slf4j
public class AspectService {

    @Pointcut("execution(* com.learing.collection.service..*.*(..)))")
    public void doInvoke() {
    }

    @Before("doInvoke()")
    public void beforeInvoke() {
        log.info("切面拦截before");
    }

    @After("doInvoke()")
    public void afterInvoke(){
        log.info("切面拦截after");
    }

    @Around("doInvoke()")
    public Object aroundInvoke(ProceedingJoinPoint joinPoint){
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            //更新执行记录
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable.getCause());
        }
    }

    @AfterReturning(returning = "result", pointcut = "doInvoke()")
    public void doAfterReturning(JoinPoint joinPoint, Object result){
        log.info("切面拦截doAfterReturning");
    }
}
