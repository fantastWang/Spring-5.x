package com.wcj.v1.servlet;

import com.wcj.v1.annotations.*;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.根据请求url，调用HandlerMapping中对应的method
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            resp.getWriter().println("500");
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, IOException {
        String requestURI = req.getRequestURI().replaceAll("/+", "/");
        //如果HandlerMapping为空，或者不存在请求的url
        if (handlerMapping.isEmpty() || !handlerMapping.containsKey(requestURI)) {
            resp.getWriter().println("404，请求地址有误");
            return;
        } else {
            Method method = handlerMapping.get(requestURI);
            Map<String, String[]> params = req.getParameterMap();
            //获取形参列表
            Class<?>[] parameterTypes = method.getParameterTypes();
            Object[] paramValues = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class paramterType = parameterTypes[i];
                if (paramterType == HttpServletResponse.class) {
                    paramValues[i] = resp;
                } else if (paramterType == String.class) {
                    //通过运行时的状态去拿到你
                    Annotation[][] pa = method.getParameterAnnotations();
                    for (int j = 0; j < pa.length; j++) {
                        for (Annotation a : pa[i]) {
                            if (a instanceof WCJRequestParam) {
                                String paramName = ((WCJRequestParam) a).value();
                                if (!"".equals(paramName.trim())) {
                                    String value = Arrays.toString(params.get(paramName)).replaceAll("\\[|\\]", "").replaceAll("\\s+", ",");
                                    paramValues[i] = value;
                                }
                            }
                        }
                    }
                }
            }
            //根据实例去进行反射调用方法
            String beanName = toLowerCase(method.getDeclaringClass().getSimpleName());
            method.invoke(ioc.get(beanName), paramValues);
        }
    }

    private void doInitIOCLoadClass() {
        if (!className.isEmpty()) {
            try {
                //将扫描的类初始化，保存到IOC容器，并不是所有类都交由Spring管理，只有Controller，Service
                for (String clz : className) {
                    Class<?> clazz = Class.forName(clz);
                    //被Controller标记的
                    if (clazz.isAnnotationPresent(WCJController.class)) {
                        Object instance = clazz.newInstance();
                        String simpleName = this.toLowerCase(clazz.getSimpleName());
                        ioc.put(simpleName, instance);
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
                        //如果接口中有多个实现类，只加载其中一个
                        for (Class interfaces : clazz.getInterfaces()) {
                            if (ioc.containsKey(interfaces.getName())) {
                                continue;
                            }
                            ioc.put(interfaces.getName(), instance);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                        }
                    }
                }
            }
        }
    }

    private void doDI() throws IllegalAccessException {
        if (!ioc.isEmpty()) {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                //获取IOC容器中的被WCJAutowired修饰的所有字段
                for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(WCJAutowired.class)) {
                        String beanName = field.getAnnotation(WCJAutowired.class).value();
                        if (StringUtils.isEmpty(beanName)) {
                            //为空则获取字段的类型
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        //给对象的字段赋值
                        field.set(entry.getValue(), ioc.get(beanName));
                    }
                }
            }
        }
    }

    private void doScannerPackage(String property) {
        //包路径本质就是文件夹，需要将com.wcj.v1替换com/wcj/v1
        String packageUrl = property.replaceAll("\\.", "/");
        //getResource返回URI
        URL url = this.getClass().getClassLoader().getResource(packageUrl);
        File file = new File(url.getFile());
        //遍历所有目录，如果是目录，递归，限制只扫描.class结尾的类文件，用于反射
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                doScannerPackage(property + "." + f.getName());
            } else {
                if (!f.getName().endsWith(".class"))
                    continue;
                //全限定类名
                className.add(property + "." + f.getName().replaceAll("\\.class", ""));
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
