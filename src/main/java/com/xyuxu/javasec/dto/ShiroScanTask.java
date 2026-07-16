package com.xyuxu.javasec.dto;

import lombok.Data;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class ShiroScanTask {

    private String id;
    private String targetUrl;

    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private String status;
    private long startTime = System.currentTimeMillis();

    // 扫描结果
    private String foundKey;
    private String cipherType;
    private String foundGadget;
    private String foundEchoType;
    private String commandResult;

    private StringBuffer logs = new StringBuffer();

    public void log(String msg) {
        String time = String.format("[%tT] ", System.currentTimeMillis());
        synchronized (this.logs) {
            this.logs.append(time).append(msg).append("\n");
        }
    }

    public String getLogsAndClear() {
        synchronized (this.logs) {
            String content = this.logs.toString();
            this.logs.setLength(0);
            return content;
        }
    }

    public boolean isTaskRunning() {
        return isRunning.get();
    }

    public void stop() {
        isRunning.set(false);
    }
}