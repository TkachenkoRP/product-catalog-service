package com.my.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.text.MessageFormat;

@Aspect
public class LoggingAspect {

    @Pointcut("within(@com.my.annotation.Loggable *) && execution(* * (..))")
    public void annotatedByLoggable() {
    }

    @Around("annotatedByLoggable()")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        System.out.println(MessageFormat.format("Calling method {0}", proceedingJoinPoint.getSignature()));
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis() - start;
        String log = MessageFormat.format("Execution of method {0} finished. Execution time is {1} ms.", proceedingJoinPoint.getSignature(), end);
        System.out.println(log);
        return result;
    }
}
