package com.xyuxu.javasec.controller;

import com.xyuxu.javasec.dto.SnakeYamlRequest;
import com.xyuxu.javasec.service.SnakeYamlService;
import com.xyuxu.javasec.service.SPIExploitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snakeyaml")
public class SnakeYamlController {

    @Autowired
    private SnakeYamlService snakeYamlService;

    @Autowired
    private SPIExploitService spiExploitService;


    /**
     * 获取支持的利用链类型 (给前端下拉框用)
     */
    @GetMapping("/types")
    public List<String> getTypes() {
        return SnakeYamlService.SUPPORTED_TYPES;
    }

    /**
     * 生成 Payload
     */
    @PostMapping("/generate")
    public Map<String, Object> generate(@RequestBody SnakeYamlRequest request) {

        Map<String, Object> resp = new HashMap<>();
        try {
            String result = snakeYamlService.generate(request);
            resp.put("success", true);
            resp.put("data", result);
            if ("ScriptEngineManager".equals(request.getType())) {
                resp.put("serverInfo", spiExploitService.getStatus());
            }
            resp.put("msg", "生成成功");
        } catch (Exception e) {
            e.printStackTrace();
            resp.put("success", false);
            resp.put("msg", "生成失败: " + e.getMessage());
        }
        return resp;
    }

    /**
     * 用于前端展示：当前是否有 HTTP 服务正在运行？端口是多少？命令是什么？
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        return spiExploitService.getStatus();
    }

    /**
     * 如果用户想释放端口，可以手动点击停止
     */
    @PostMapping("/stop")
    public Map<String, Object> stopServer() {
        spiExploitService.stop();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("msg", "SPI HTTP 服务已停止");
        return resp;
    }
}