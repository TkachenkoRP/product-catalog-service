package com.my.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Аспект для логирования выполнения методов, помеченных аннотацией {@link com.my.annotation.Loggable}.
 * Регистрирует начало и окончание выполнения метода, а также измеряет время его работы.
 */
@Aspect
@Component
@Slf4j
public class LoggerAspect {
    /**
     * Точка среза для методов, помеченных аннотацией {@link com.my.annotation.Loggable}.
     */
    @Pointcut("within(@com.my.annotation.Loggable *) && execution(* * (..))")
    public void annotatedByLoggable() {
    }

    /**
     * Совет типа "вокруг" для логирования выполнения методов.
     * <p>
     * Логирует начало выполнения метода, время его работы и завершение.
     * </p>
     *
     * @param proceedingJoinPoint точка присоединения, представляющая выполняемый метод
     * @return результат выполнения метода
     * @throws Throwable если выполнение метода вызывает исключение
     */
    @Around("annotatedByLoggable()")
    public Object logging(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        log.info("Calling method {}", proceedingJoinPoint.getSignature());
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        long end = System.currentTimeMillis() - start;
        log.info("Execution of method {} finished. Execution time is {} ms.", proceedingJoinPoint.getSignature(), end);
        return result;
    }
}
