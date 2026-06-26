package com.mojian.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用户端操作日志注解
 * 用于记录用户端的所有操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserLogger {

    /**
     * 操作名称
     */
    String value() default "";

    /**
     * 模块名称
     */
    String module() default "";
}
