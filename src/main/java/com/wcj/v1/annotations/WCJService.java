package com.wcj.v1.annotations;

import java.lang.annotation.*;

/**
 * @author wangchaojie
 * @Description 自定义注解-Service
 * @Date 2020/7/24 9:52
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WCJService {
    String value() default "";
}
