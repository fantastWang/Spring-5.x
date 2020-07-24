package com.wcj.v1.service.impl;

import com.wcj.v1.annotations.WCJService;
import com.wcj.v1.service.IDemoService;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/24 13:50
 **/
@WCJService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String sayHello(String name) {
        return "Hello," + name;
    }
}
