package com.wcj.annotationsCoding;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/27 10:07
 **/
public class PersonBak implements InitializingBean, DisposableBean {
    @Override
    public void destroy() throws Exception {
        System.out.println("PersonBak destroy");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("PersonBak begin");
    }
}
