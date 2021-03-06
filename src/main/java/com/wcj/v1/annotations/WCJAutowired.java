package com.wcj.v1.annotations;

import java.lang.annotation.*;

/**
 * @author wangchaojie
 * @Description 自定义注解-Autowired
 * @Date 2020/7/24 9:52
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WCJAutowired {
    String value() default "";
}
