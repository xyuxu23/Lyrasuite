package com.xyuxu.javasec.controller;

import com.xyuxu.javasec.core.payload.jndi.BypassFactory;
import com.xyuxu.javasec.core.payload.jndi.JNDIType;
import com.xyuxu.javasec.utils.NetUtil;
import com.xyuxu.javasec.dto.JndiRequest;
import com.xyuxu.javasec.service.JndiManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jndi")
public class JndiController {

    @Autowired
    private JndiManageService jndiService;

    /**
     *
     * POST /api/jndi/start
     */
    @PostMapping("/start")
    public Map<String, Object> start(@RequestBody JndiRequest request) {
        Map<String, Object> resp = new HashMap<>();
        try {
            JndiManageService.JndiTask task = jndiService.startServer(request);
            resp.put("success", true);
            resp.put("taskId", task.id);
            resp.put("rmiPort", task.rmiPort);
            resp.put("ldapPort", task.ldapPort);
            return resp;
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("msg", e.getMessage());
            return resp;
        }
    }

    @GetMapping("/active-task")
    public Map<String, Object> getActiveTask() {
        Map<String, Object> resp = new HashMap<>();
        JndiManageService.JndiTask task = jndiService.getActiveTask();
        if (task != null) {
            resp.put("active", true);
            resp.put("taskId", task.id);
            resp.put("request", task.request);
            resp.put("rmiPort", task.rmiPort);
            resp.put("ldapPort", task.ldapPort);
        } else {
            resp.put("active", false);
        }
        return resp;
    }

    /**
     * 停止服务
     */
    @PostMapping("/stop")
    public Map<String, Object> stop(@RequestBody Map<String, String> body) {
        String taskId = body.get("taskId");
        jndiService.stopServer(taskId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("msg", "服务已停止");
        return resp;
    }

    /**
     * 获取状态和日志
     * GET /api/jndi/status?taskId=xxxx-xxxx
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus(@RequestParam String taskId) {
        Map<String, Object> resp = new HashMap<>();

        String logs = jndiService.fetchLog(taskId);

        if (logs == null) {
            resp.put("running", false);
            resp.put("logs", "");
        } else {
            resp.put("running", true);
            resp.put("logs", logs);
        }
        return resp;
    }

    /**
     * 初始化数据接口
     * GET /api/jndi/meta
     */
    @GetMapping("/meta")
    public Map<String, Object> getMetadata() {
        Map<String, Object> data = new HashMap<>();

        // 获取本机 IP (辅助填入)
        data.put("localIp", NetUtil.getRealIp());

        // 获取所有 JNDI 类型枚举
        data.put("jndiTypes", JNDIType.values());

        // 获取 Bypass 类型列表
        data.put("bypassTypes", BypassFactory.getAllTypes());

        return data;
    }
}