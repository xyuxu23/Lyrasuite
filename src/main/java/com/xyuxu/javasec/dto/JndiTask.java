package com.xyuxu.javasec.dto;

import com.xyuxu.javasec.service.JNDIExploitService;
import com.xyuxu.javasec.utils.LogUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class JndiTask {

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
