package com.wcj.annotationsCoding;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/27 8:57
 **/
public class MyFactoryBean implements FactoryBean {
    @Override
    public Object getObject() throws Exception {
        return new Person();
    }

    @Override
    public Class<?> getObjectType() {
        return Person.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
