package com.xyuxu.javasec.controller;

import com.xyuxu.javasec.core.payload.gadgets.ObjectPayload;
import com.xyuxu.javasec.utils.PayloadUtils;
import com.xyuxu.javasec.dto.GadgetRequest;
import com.xyuxu.javasec.service.GadgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gadget")
public class GadgetController {

    @Autowired
    private GadgetService gadgetService;

    /**
     * 获取支持的 Gadget 列表
     */
    @GetMapping("/types")
    public List<String> getGadgetTypes() {
        try {
            // 1. 获取所有 Payload 类
            Set<Class<? extends ObjectPayload>> payloadClasses = PayloadUtils.getAllPayloadClasses();
            return payloadClasses.stream()
                    .map(Class::getSimpleName)
                    .sorted((s1, s2) -> {
                        int p1 = getGadgetPriority(s1);
                        int p2 = getGadgetPriority(s2);
                        if (p1 != p2) {
                            return Integer.compare(p1, p2);
                        }
                        return s1.compareTo(s2);
                    })
                    .collect(Collectors.toList());

        } catch (Throwable t) {
            t.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * 生成 Payload 接口
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@RequestBody GadgetRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String result = gadgetService.generatePayload(request);

            response.put("success", true);
            response.put("data", result);
            response.put("message", "生成成功: " + request.getGadget());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "生成失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    /**
     * 获取帮助信息接口
     */
    @GetMapping("/help")
    public ResponseEntity<String> getHelp(@RequestParam String name) {
        String helpText = gadgetService.getGadgetHelp(name);
        return ResponseEntity.ok(helpText != null ? helpText : "");
    }


    private int getGadgetPriority(String name) {
        if (name.startsWith("URLDNS")) return 1;
        if (name.startsWith("FindClass")) return 2;
        if (name.startsWith("Spring")) return 3;
        if (name.startsWith("CommonsBeanutils")) return 5;
        if (name.startsWith("CommonsCollections")) return 4;
        return 99;
    }
}