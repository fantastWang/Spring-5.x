package com.wcj.annotationsCoding;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/27 8:57
 **/
public class PersonCopy {

    @PostConstruct
    public void start() {
        System.out.println("PersonCopy start");
    }

    @PreDestroy
    public void end() {
        System.out.println("PersonCopy end");
    }
}
