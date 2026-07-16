package com.xyuxu.javasec.utils;

import java.text.SimpleDateFormat;
import java.util.Date;


public class LogUtils {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm:ss");

    /**
     * 给消息加上时间戳
     * @param msg 原始消息
     * @return [12:00:00] 原始消息
     */
    public static String format(String msg) {
        if (msg == null) return "";
        if (msg.startsWith("-") || msg.trim().isEmpty()) {
            return msg;
        }
        String time = SDF.format(new Date());
        return String.format("[%s] %s", time, msg);
    }
}