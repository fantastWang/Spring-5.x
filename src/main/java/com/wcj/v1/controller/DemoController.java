package com.wcj.v1.controller;

import com.wcj.v1.annotations.WCJAutowired;
import com.wcj.v1.annotations.WCJController;
import com.wcj.v1.annotations.WCJRequestMapping;
import com.wcj.v1.annotations.WCJRequestParam;
import com.wcj.v1.service.IDemoService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/24 10:50
 **/
@WCJController
@WCJRequestMapping("/demo")
public class DemoController {

    @WCJAutowired
    private IDemoService testService;

    @WCJRequestMapping("/sayHello")
    public void sayHello(HttpServletResponse resp, @WCJRequestParam("name") String name) throws IOException {
        String sayHello = testService.sayHello(name);
        resp.getWriter().println(sayHello);
        resp.getWriter().println(name);
    }
}
