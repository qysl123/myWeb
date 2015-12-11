package com.zk.Interceptor;

import com.zk.entity.TestVO;
import com.zk.redis.dao.RedisDao;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Aspect
public class MyInterceptor {

    @Resource
    private RedisDao redisDao;

    @Pointcut("execution(* com.zk.service.impl.TestServiceImpl.getTest(..))")
    private void getMethod(){}//定义一个切入点
    @Pointcut("execution(* com.zk.service.impl.TestServiceImpl.saveTest(..))")
    private void addMethod(){}//定义一个切入点

//    @Before("getMethod()")
//    public void doAccessCheck(){
//        System.out.println("前置通知");
//    }

//    @AfterReturning("addMethod()&&args(testVO)")
//    public void doAfter(TestVO testVO){
//        System.out.println("更新数据时,同时更新缓存");
//        redisDao.saveTest(testVO);
//    }

//    @After("anyMethod()")
//    public void after(){
//        System.out.println("最终通知");
//    }

    // 定义环绕通知
    @Around("getMethod()")
    public Object getAround(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("通过缓存查询");
        String id = (String) pjp.getArgs()[0];
        TestVO testVO = redisDao.getTest(id);
        if(testVO == null){
            System.out.println("缓存查询失败, 通过mysql查询");
            testVO = (TestVO) pjp.proceed();
        }
        return testVO;
    }

    // 定义环绕通知
    @Around("addMethod()")
    public Object addAround(ProceedingJoinPoint pjp) throws Throwable {
        Object object = pjp.proceed();
        System.out.println("更新数据时,同时更新缓存");
        redisDao.saveTest((TestVO) pjp.getArgs()[0]);
        return object;
    }
}
