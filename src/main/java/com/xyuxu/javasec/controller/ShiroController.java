package com.xyuxu.javasec.controller;

import com.xyuxu.javasec.core.generator.EchoByteCodeGenerator;
import com.xyuxu.javasec.utils.PayloadUtils;
import com.xyuxu.javasec.dto.ShiroRequest;
import com.xyuxu.javasec.dto.ShiroScanTask;
import com.xyuxu.javasec.service.ShiroExploitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shiro")
public class ShiroController {

    @Autowired
    private ShiroExploitService shiroService;

    /**
     * 开始扫描任务 (Key 扫描 或 Gadget 扫描)
     */
    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody ShiroRequest req) {
        // 基础参数校验
        if (req.getUrl() == null || !req.getUrl().startsWith("http")) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("msg", "无效的 URL 地址");
            return err;
        }

        try {
            String taskId = shiroService.startScan(req);
            return Map.of("success", true, "taskId", taskId);

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("msg", "服务器内部错误: " + e.getMessage());
            return err;
        }
    }

    /**
     * 命令执行
     */
    @PostMapping("/exec")
    public Map<String, Object> exec(@RequestBody ShiroRequest req) {
        if (req.getSpecifiedKey() == null ||
                req.getSpecifiedGadget() == null ||
                req.getSpecifiedEcho() == null) {
            return Map.of("success", false, "msg", "执行命令需要完整的 Key/Gadget/Echo 配置");
        }

        try {
            String result = shiroService.executeCommand(req);
            return Map.of("success", true, "data", result);
        } catch (Exception e) {
            return Map.of("success", false, "msg", "执行失败: " + e.getMessage());
        }
    }


    /**
     * 获取支持的 Gadget 列表
     */
    @GetMapping("/gadgets")
    public List<String> getShiroGadgets() {
        return PayloadUtils.getBytecodePayloadClasses().stream()
                .map(Class::getSimpleName)
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 获取支持的回显类型列表
     */
    @GetMapping("/echos")
    public List<String> getEchoTypes() {
        return EchoByteCodeGenerator.getEchoTypeNames();
    }

    /**
     * 获取任务状态和实时日志
     */
    @GetMapping("/status")
    public Map<String, Object> status(@RequestParam String taskId) {
        ShiroScanTask task = shiroService.getTaskStatus(taskId);
        if (task == null) return Map.of("running", false, "msg", "任务不存在");

        Map<String, Object> response = new HashMap<>();
        response.put("running", task.isTaskRunning());
        response.put("status", task.getStatus());
        response.put("logs", task.getLogsAndClear());

        if (task.getFoundKey() != null) {
            response.put("key", task.getFoundKey());
            response.put("cipherType", task.getCipherType());
        }
        if (task.getFoundGadget() != null) {
            response.put("foundGadget", task.getFoundGadget());
            response.put("foundEchoType", task.getFoundEchoType());
            response.put("commandResult", task.getCommandResult());
        }
        return response;
    }

    /**
     * 停止任务
     */
    @PostMapping("/stop")
    public Map<String, Object> stop(@RequestBody Map<String, String> body) {
        shiroService.stopTask(body.get("taskId"));
        return Map.of("success", true);
    }
}