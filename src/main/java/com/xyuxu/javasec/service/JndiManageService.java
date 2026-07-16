package com.xyuxu.javasec.service;

import com.xyuxu.javasec.core.payload.jndi.JNDIType;
import com.xyuxu.javasec.utils.LogUtils;
import com.xyuxu.javasec.utils.NetUtil;
import com.xyuxu.javasec.dto.JndiRequest;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JndiManageService {

    private JndiTask activeTask = null;

    public JndiTask startServer(JndiRequest req) throws Exception {
        if (activeTask != null) throw new Exception("已有任务正在运行，请先停止。");

        String taskId = UUID.randomUUID().toString();
        JndiTask task = new JndiTask(taskId, req);

        int rmi = 0, ldap = 0, http = 0;
        if (req.getType() == JNDIType.REMOTE_REFERENCE) {
            rmi = NetUtil.getAvailablePort();
            ldap = NetUtil.getAvailablePort();
            http = NetUtil.getAvailablePort();
        } else if (req.getType() == JNDIType.LDAP_SERIALIZED) {
            ldap = NetUtil.getAvailablePort();
        } else if (req.getType() == JNDIType.RMI_LOCAL_FACTORY) {
            rmi = NetUtil.getAvailablePort();
        }

        task.rmiPort = rmi; task.ldapPort = ldap; task.httpPort = http;
        task.service = new JNDIExploitService(task::log);
        task.log("正在启动服务 (ID: " + taskId + ")...");

        task.service.start(req.getIp(), rmi, ldap, http, req.getType(),
                req.getCommand(), req.getGadget(), req.getBypassType());

        this.activeTask = task;
        return task;
    }

    public void stopServer(String taskId) {
        if (activeTask != null && activeTask.id.equals(taskId)) {
            activeTask.service.stop();
            activeTask = null;
        }
    }

    public JndiTask getActiveTask() { return activeTask; }

    public String fetchLog(String taskId) {
        return (activeTask != null && activeTask.id.equals(taskId)) ? activeTask.getAndClearLog() : null;
    }

    public static class JndiTask {
        public String id;
        public int rmiPort, ldapPort, httpPort;
        public JndiRequest request;
        JNDIExploitService service;
        private final StringBuffer logBuffer = new StringBuffer();

        public JndiTask(String id, JndiRequest req) { this.id = id; this.request = req; }
        public void log(String msg) {
            synchronized (logBuffer) { logBuffer.append(LogUtils.format(msg)).append("\n"); }
        }
        public String getAndClearLog() {
            synchronized (logBuffer) {
                String logs = logBuffer.toString();
                logBuffer.setLength(0);
                return logs;
            }
        }
    }
}