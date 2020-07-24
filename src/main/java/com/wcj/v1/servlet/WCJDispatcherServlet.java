package com.wcj.v1.servlet;

import com.wcj.v1.annotations.WCJController;
import com.wcj.v1.annotations.WCJRequestMapping;
import com.wcj.v1.annotations.WCJService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author wangchaojie
 * @Description 自定义DispatcherServlet
 * @Date 2020/7/20 10:17
 **/
public class WCJDispatcherServlet extends HttpServlet {
    private Properties properties = new Properties();
    private List<String> className = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();
    private Map<String, Method> handlerMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.根据请求url，查找HandlerMapping中与之一一对应的method
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            resp.getWriter().println("500");
            e.printStackTrace();
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException {
        String requestURI = req.getRequestURI().replaceAll("/+", "/");
        if (!handlerMapping.isEmpty()) {
            Method method = handlerMapping.get(requestURI);
            method.invoke(null, null);
        }
    }

    @Override
    public void init(ServletConfig config) {
        //1.读取配置文件
        doReadProperties(config.getInitParameter("contextConfigLocation"));
        //2.扫描包下的所有类
        doScannerPackage(properties.getProperty("scan-package"));
        //3.初始化IOC容器,将扫描的类保存至IOC容器
        doInitIOCLoadClass();
        //4.进行DI操作，扫描IOC容器中的实例，将没有复制赋值对象进行赋值
        doDI();
        //5.初始化HandlerMapping，完成method和url一对一映射
        doInitHandlerMapping();
    }

    private void doInitIOCLoadClass() {
        if (className.isEmpty())
            return;
        try {
            //将扫描的类初始化，保存到IOC容器，并不是所有类都交由Spring管理，只有Controller，Service
            for (String clz : className) {
                Class<?> clazz = Class.forName(clz);
                //被Controller标记的
                if (clazz.isAnnotationPresent(WCJController.class)) {
                    Object instance = clazz.newInstance();
                    String simpleName = this.toLowerCase(clazz.getSimpleName());
                    ioc.put(simpleName, instance);
                    System.out.println("controller ioc:" + simpleName + ":" + instance);
                }
                //被Service标记的
                else if (clazz.isAnnotationPresent(WCJService.class)) {
                    //处理可能在不同包下出现相同的类名，根据@Service("")中的名字区分
                    String beanName = clazz.getAnnotation(WCJService.class).value();
                    if (StringUtils.isEmpty(beanName)) {
                        beanName = toLowerCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    System.out.println("service ioc:" + beanName + ":" + instance);
                    //如果接口中有多个实现类，只加载其中一个
                    for (Class interfaces : clazz.getInterfaces()) {
                        if (ioc.containsKey(interfaces.getName())) {
                            continue;
                        }
                        ioc.put(interfaces.getName(), instance);
                        System.out.println("service interface ioc:" + interfaces.getName() + ":" + instance);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doInitHandlerMapping() {
        if (!ioc.isEmpty()) {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<?> clz = entry.getValue().getClass();
                //只过滤被Controller修饰的类
                if (clz.isAnnotationPresent(WCJController.class)) {
                    String baseUrl = "";
                    //获取类上的RequestMapping
                    if (clz.isAnnotationPresent(WCJRequestMapping.class)) {
                        baseUrl = clz.getAnnotation(WCJRequestMapping.class).value();
                    }
                    //获取方法上的RequestMapping（不限访问修饰符）
                    for (Method method : clz.getDeclaredMethods()) {
                        //只有被RequestMapping修饰的，才存入HandlerMapping
                        if (method.isAnnotationPresent(WCJRequestMapping.class)) {
                            String url = method.getAnnotation(WCJRequestMapping.class).value();
                            url = ("/" + baseUrl + "/" + url).replaceAll("/+", "/");
                            handlerMapping.put(url, method);
                            System.out.println("handlerMapping:" + url + ":" + method);
                        }
                    }
                }
            }
        }
    }

    private void doDI() {
        if (!ioc.isEmpty()) {

        }
    }

    private void doScannerPackage(String property) {
        //包路径本质就是文件夹，需要将com.wcj.v1替换com/wcj/v1
        property = property.replaceAll("\\.", "/");
        //getResource返回URI
        URL url = this.getClass().getClassLoader().getResource(property);
        File file = new File(url.getFile());
        //遍历所有目录，如果是目录，递归，限制只扫描.class结尾的类文件，用于反射
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                doScannerPackage(property + "." + f.getName());
            } else {
                if (!f.getName().endsWith(".class"))
                    continue;
                className.add(f.getName());
                System.out.println("className:" + f.getName());
            }
        }
    }

    //转换成首字母小写
    private String toLowerCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doReadProperties(String contextConfigLocation) {
        contextConfigLocation = contextConfigLocation.split("classpath:")[1];
        //getResourceAsStream返回流, 指定路径的资源转换为流使用
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
