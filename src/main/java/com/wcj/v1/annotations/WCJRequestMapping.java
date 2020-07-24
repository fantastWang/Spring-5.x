package com.wcj.v1.annotations;

import java.lang.annotation.*;

/**
 * @author wangchaojie
 * @Description 自定义注解-RequestMapping
 * @Date 2020/7/24 9:52
 **/
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WCJRequestMapping {
    String value() default "";
}
