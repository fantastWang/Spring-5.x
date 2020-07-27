package com.wcj.annotationsCoding;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/27 8:58
 **/
public class MyTest {

    @Test
    public void test1() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(MyConfig.class);
        context.close();
    }
}
