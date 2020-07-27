package com.wcj.annotationsCoding;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/27 8:57
 **/
public class PersonCut implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("前：" + beanName + "," + bean);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.println("后：" + beanName + "," + bean);
        return bean;
    }
}
