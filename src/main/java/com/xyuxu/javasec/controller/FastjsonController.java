package com.xyuxu.javasec.controller;

import com.xyuxu.javasec.dto.FastjsonRequest;
import com.xyuxu.javasec.service.FastjsonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fastjson")
public class FastjsonController {

    @Autowired
    private FastjsonService fastjsonService;

    /**
     * 获取支持的 Payload 类型列表
     */
    @GetMapping("/types")
    public List<String> getTypes() {
        return FastjsonService.SUPPORTED_TYPES;
    }

    /**
     * 生成 Payload
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody FastjsonRequest request) {
        Map<String, Object> resp = new HashMap<>();
        try {
            String result = fastjsonService.generate(request);

            resp.put("success", true);
            resp.put("data", result);
            resp.put("msg", "生成成功: " + request.getType());
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("msg", "生成失败: " + e.getMessage());
        }
        return resp;
    }
}