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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author wangchaojie
 * @Description TODO
 * @Date 2020/7/25 9:51
 **/
public class WCJDispatcherServletBak extends HttpServlet {
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
        String requestURI = req.getRequestURI();
        if (handlerMapping.isEmpty() || !handlerMapping.containsKey(requestURI)) {
            resp.getWriter().println("500");
        } else {
            Method method = handlerMapping.get(requestURI);
            String beanName = toLowerCase(method.getDeclaringClass().getSimpleName());
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
                                    String value = Arrays.toString(params.get(paramName))
                                            .replaceAll("\\[|\\]", "")
                                            .replaceAll("\\s+", ",");
                                    paramValues[i] = value;
                                }
                            }
                        }
                    }
                }
            }
            try {
                method.invoke(ioc.get(beanName), paramValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        doLoadProperties(config.getInitParameter("contextConfigLocation"));
        doScanner(properties.getProperty("scan-package"));
        doInitIOC();
        doDI();
        doInitHandlerMapping();
    }

    private void doInitHandlerMapping() {
        if (!ioc.isEmpty()) {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Class<?> clazz = entry.getValue().getClass();
                String baseUrl = "";
                if (clazz.isAnnotationPresent(WCJController.class)) {
                    baseUrl = clazz.getAnnotation(WCJRequestMapping.class).value();
                }
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(WCJRequestMapping.class)) {
                        String value = method.getAnnotation(WCJRequestMapping.class).value();
                        value = baseUrl + value;
                        handlerMapping.put(value.replaceAll("/+", "/"), method);
                    }
                }
            }
        }
    }

    private void doDI() {
        if (!ioc.isEmpty()) {
            try {
                for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                    for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                        if (field.isAnnotationPresent(WCJAutowired.class)) {
                            String value = field.getAnnotation(WCJAutowired.class).value();
                            if (StringUtils.isEmpty(value)) {
                                value = toLowerCase(field.getType().getSimpleName());
                            }
                            field.setAccessible(true);
                            field.set(entry.getValue(), ioc.get(value));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doInitIOC() {
        if (!className.isEmpty()) {
            try {
                for (String clazz : className) {
                    Class<?> aClass = Class.forName(clazz);
                    if (aClass.isAnnotationPresent(WCJController.class)) {
                        Object instance = aClass.newInstance();
                        String simpleName = toLowerCase(instance.getClass().getSimpleName());
                        ioc.put(simpleName, instance);
                    } else if (aClass.isAnnotationPresent(WCJService.class)) {
                        Object instance = aClass.newInstance();
                        String simpleName = toLowerCase(instance.getClass().getSimpleName());
                        ioc.put(simpleName, instance);
                        for (Class<?> classInterface : aClass.getInterfaces()) {
                            if (ioc.containsKey(classInterface.getSimpleName())) {
                                continue;
                            } else {
                                ioc.put(toLowerCase(classInterface.getSimpleName()), instance);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String toLowerCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String property) {
        String baseUrl = property.replace(".", "/");
        URL url = this.getClass().getClassLoader().getResource(baseUrl);
        try {
            File file = new File(url.getFile());
            for (File listFile : file.listFiles()) {
                if (listFile.isDirectory()) {
                    doScanner(property + "." + listFile.getName());
                } else {
                    className.add((property + "." + listFile.getName()).replaceAll("\\.class", ""));
                    System.out.println((property + "." + listFile.getName()).replaceAll("\\.class", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadProperties(String contextConfigLocation) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.split("classpath:")[1]);
        try {
            properties.load(resourceAsStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
