package com.my.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки методов или классов, требующих аудита.
 * Методы или классы, помеченные этой аннотацией, будут перехватываться
 * аспектом {@link com.my.aspect.AuditionAspect} для записи информации
 * о вызовах в лог аудита.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audition {
}
