package com.xyuxu.javasec.service;

import com.xyuxu.javasec.core.generator.GadgetsGenerator;
import com.xyuxu.javasec.utils.ProbeUtil;
import com.xyuxu.javasec.dto.GadgetRequest;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class GadgetService {

    /**
     * 核心生成逻辑
     */
    public String generatePayload(GadgetRequest request) throws Exception {
        // 1. 参数校验
        if (request.getGadget() == null || request.getGadget().isEmpty()) {
            throw new IllegalArgumentException("未选择利用链 (Gadget)");
        }
        if (request.getCommand() == null || request.getCommand().trim().isEmpty()) {
            throw new IllegalArgumentException("命令不能为空");
        }

        // 2. 调用 Core 中的生成器 (复用你原本的代码)
        byte[] serializedBytes = GadgetsGenerator.generateBytes(
                request.getGadget(),
                request.getCommand(),
                request.isDirtyData()
        );

        // 3. 根据编码格式处理输出
        return encodeBytes(serializedBytes, request.getEncoding());
    }

    /**
     * 获取 Gadget 的帮助信息
     */
    public String getGadgetHelp(String gadgetName) {
        return ProbeUtil.getHelpText(gadgetName);
    }

    /**
     * 编码辅助方法
     */
    private String encodeBytes(byte[] data, String encoding) {
        if (data == null) return "";

        String enc = (encoding == null) ? "Base64" : encoding;

        switch (enc.toLowerCase()) {
            case "hex":
                return HexFormat.of().formatHex(data);
            case "none":
                return new String(data, StandardCharsets.ISO_8859_1);
            case "base64":
            default:
                return Base64.getEncoder().encodeToString(data);
        }
    }
}