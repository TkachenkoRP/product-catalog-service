package com.my.aspect;

import com.my.security.UserManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.stereotype.Component;

/**
 * Аспект для аудита выполнения методов, помеченных аннотацией {@link com.my.annotation.Audition}.
 * Записывает в лог информацию о вызовах методов с указанием пользователя,
 * выполняющего операцию, и результатов выполнения.
 */
@Aspect
@Component
@Slf4j
public class AuditionAspect {
    /**
     * Маркер для идентификации записей аудита в логах.
     */
    private final Marker marker = MarkerFactory.getMarker("AUDITION");

    /**
     * Точка среза для методов, помеченных аннотацией {@link com.my.annotation.Audition}.
     */
    @Pointcut("within(@com.my.annotation.Audition *) && execution(public * * (..))")
    public void annotatedByAudition() {
    }

    /**
     * Совет типа "перед" для логирования начала выполнения метода.
     * Записывает информацию о пользователе и вызываемом методе.
     *
     * @param joinPoint точка присоединения, представляющая выполняемый метод
     */
    @Before("annotatedByAudition()")
    public void before(JoinPoint joinPoint) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UserManager.getLoggedInUser() == null ? "No User: " : UserManager.getLoggedInUser().getId() + " - " + UserManager.getLoggedInUser().getEmail() + ": ");
        stringBuilder.append("Calling method ").append(joinPoint.getSignature());
        log.info(marker, stringBuilder.toString());
    }

    /**
     * Совет типа "после возврата" для логирования успешного завершения метода.
     * Записывает информацию о пользователе и завершенном методе.
     *
     * @param joinPoint точка присоединения, представляющая выполненный метод
     * @param result результат выполнения метода
     */
    @AfterReturning(pointcut = "annotatedByAudition()", returning = "result")
    public void after(JoinPoint joinPoint, Object result) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UserManager.getLoggedInUser() == null ? "No User: " : UserManager.getLoggedInUser().getId() + " - " + UserManager.getLoggedInUser().getEmail() + ": ");
        stringBuilder.append("Execution of method ").append(joinPoint.getSignature());
        log.info(marker, stringBuilder.toString());
    }
}
