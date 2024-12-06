package com.qq2008.common.annotation;

import java.lang.annotation.*;

/***
 * 允许未登录角色者访问
 */
@Documented // 会被 javadoc 之类的工具处理
@Inherited // 允许子类继承父类的注解
@Retention(RetentionPolicy.RUNTIME) // 注解可在运行时保留
@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.PACKAGE, ElementType.TYPE}) // 注解所修饰的对象范围
public @interface SkipRole {
    boolean validate() default true;
}
