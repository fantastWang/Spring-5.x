package com.wcj.v1.annotations;

import java.lang.annotation.*;

/**
 * @author wangchaojie
 * @Description 自定义注解-RequestParam
 * @Date 2020/7/24 11:14
 **/
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WCJRequestParam {
    String value() default "";
}
