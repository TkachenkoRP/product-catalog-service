package com.my.aspect;

import com.my.security.UserManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class AuditAspect {
    @Pointcut("within(@com.my.annotation.Audition *) && execution(public * * (..))")
    public void annotatedByAudition() {
    }

    @Before("annotatedByAudition()")
    public void before(JoinPoint joinPoint) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UserManager.getLoggedInUser() == null ? "No User: " : UserManager.getLoggedInUser().getId() + " - " + UserManager.getLoggedInUser().getEmail() + ": ");
        stringBuilder.append("Calling method ").append(joinPoint.getSignature());
        System.out.println(stringBuilder);
    }

    @After("annotatedByAudition()")
    public void after(JoinPoint joinPoint) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(UserManager.getLoggedInUser() == null ? "No User: " : UserManager.getLoggedInUser().getId() + " - " + UserManager.getLoggedInUser().getEmail() + ": ");
        stringBuilder.append("Execution of method ").append(joinPoint.getSignature());
        System.out.println(stringBuilder);
    }
}
