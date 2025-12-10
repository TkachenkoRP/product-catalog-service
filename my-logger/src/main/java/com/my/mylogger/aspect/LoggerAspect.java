package com.my.mylogger.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;

/**
 * Аспект для логирования выполнения методов, помеченных аннотацией {@link com.my.mylogger.annotation.Loggable}.
 * Для использования необходимо пометить метод или класс аннотацией {@code @Loggable}.
 **/
@Aspect
@Component
public class LoggerAspect {
    private final Logger log = LoggerFactory.getLogger(LoggerAspect.class);
    private final Marker marker = MarkerFactory.getMarker("LOGGER");

    @Pointcut("within(@com.my.mylogger.annotation.Loggable *) && execution(* * (..))")
    public void annotatedByLoggable() {
    }

    @Around("annotatedByLoggable()")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info(marker, "Calling method {}", proceedingJoinPoint.getSignature());
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis() - start;
        log.info(marker, "Execution of method {} finished. Execution time is {} ms.", proceedingJoinPoint.getSignature(), end);
        return result;
    }
}
