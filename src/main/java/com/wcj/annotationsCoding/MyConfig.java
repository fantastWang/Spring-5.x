package com.wcj.annotationsCoding;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/27 8:58
 **/
@Configuration
public class MyConfig {

    @Bean(initMethod = "start", destroyMethod = "end")
    public Person person() {
        return new Person();
    }

    @Bean
    @DependsOn("person")
    public PersonBak personBak() {
        return new PersonBak();
    }

//    @Bean
//    public PersonCopy personCopy() {
//        return new PersonCopy();
//    }
//
//    @Bean
//    public PersonCut personCut() {
//        return new PersonCut();
//    }
}
