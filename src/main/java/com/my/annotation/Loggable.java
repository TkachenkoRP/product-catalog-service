package com.my.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки методов или классов, требующих логирования.
 * Методы или классы, помеченные этой аннотацией, будут перехватываться
 * аспектом {@link com.my.aspect.LoggerAspect} для логирования времени выполнения.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Loggable {
}
