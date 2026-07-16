package com.xyuxu.javasec.service;

import com.xyuxu.javasec.core.generator.FastjsonPaylodGenerator;
import com.xyuxu.javasec.dto.FastjsonRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FastjsonService {

    public static final List<String> SUPPORTED_TYPES = Arrays.asList(
            "TemplatesImpl",
            "C3P0(Hex)",
            "JdbcRowSetImpl",
            "JdbcRowSetImpl(Bypass)",
            "JndiDataSourceFactory",
            "SpringPropertyPath",
            "SpringAdvisor",
            "C3P0JndiRef",
            "DNS(InetAddress)",
            "DNS(Inet6Address)",
            "DNS(URL)"
    );

    public String generate(FastjsonRequest req) throws Exception {
        String type = req.getType();
        String command = req.getCommand();
        String jndiUrl = req.getJndiUrl();
        String echoType= req.getEchoType();

        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("未选择 Payload 类型");
        }
        // 1. TemplatesImpl -> 需要 Command
        if (type.equals("TemplatesImpl")) {
            return FastjsonPaylodGenerator.generateTemplatesImpl(command,echoType);
        }
        // 2. DNS / Hex -> 需要 Command
        else if (type.startsWith("DNS") || type.equals("C3P0(Hex)")) {
            validateInput(command, "Command");

            if (type.equals("DNS(InetAddress)")) return FastjsonPaylodGenerator.generateDnsInetAddress(command);
            if (type.equals("DNS(Inet6Address)")) return FastjsonPaylodGenerator.generateDnsInet6Address(command);
            if (type.equals("DNS(URL)")) return FastjsonPaylodGenerator.generateDnsURL(command);
            if (type.equals("C3P0(Hex)")) return FastjsonPaylodGenerator.generateC3P0Hex(command);
        }

        // 3. JNDI 类型 -> 需要 JNDI URL
        else {
            validateInput(jndiUrl, "JNDI URL");
            if (type.equals("JdbcRowSetImpl")) return FastjsonPaylodGenerator.generateJdbcRowSetImpl(jndiUrl);
            if (type.equals("JdbcRowSetImpl(Bypass)")) return FastjsonPaylodGenerator.generateJdbcRowSetImplBypass47(jndiUrl);
            if (type.equals("JndiDataSourceFactory")) return FastjsonPaylodGenerator.generateJndiDataSourceFactory(jndiUrl);
            if (type.equals("SpringPropertyPath")) return FastjsonPaylodGenerator.generateSpringPropertyPath(jndiUrl);
            if (type.equals("SpringAdvisor")) return FastjsonPaylodGenerator.generateSpringAdvisor(jndiUrl);
            if (type.equals("C3P0JndiRef")) return FastjsonPaylodGenerator.generateC3P0Jndi(jndiUrl);
        }
        throw new IllegalArgumentException("未知的 Payload 类型: " + type);
    }

    private void validateInput(String val, String fieldName) {
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
    }
}