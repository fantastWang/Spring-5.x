package com.wcj.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/20 10:17
 **/
public class WCJDispatcherServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //1.读取配置文件
        doReadProperties();
        //2.初始化IOC容器
        doInitIOC();
        //3.扫描包下的所有类
        doScannerPackage();
        //4.将类进行初始化，加载到IOC容器中
        doLoadClass();
        //5.进行DI操作，扫描IOC容器中的实例，将没有复制赋值对象进行赋值
        doDI();
        //6.初始化HandlerMapping，完成对象和url一对一映射
        doInitHandlerMapping();
    }

    private void doInitHandlerMapping() {
    }

    private void doDI() {
    }

    private void doLoadClass() {
    }

    private void doScannerPackage() {
    }

    private void doInitIOC() {
    }

    private void doReadProperties() {
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }
}
