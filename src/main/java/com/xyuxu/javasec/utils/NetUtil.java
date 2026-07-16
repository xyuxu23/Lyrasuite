package com.xyuxu.javasec.utils;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

public class NetUtil {
    /**
     * 获取本机真实IP
     */
    public static String getRealIp() {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (intf.isLoopback() || !intf.isUp()) continue;
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (addr.isLoopbackAddress()) continue;
                    String ip = addr.getHostAddress();
                    if (ip.contains(":")) continue;
                    return ip;
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }

    public static int getAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("系统资源耗尽，无法分配端口", e);
        }
    }

    private static final HostnameVerifier allHostsValid = (hostname, session) -> true;
    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };

    /**
     * 核心请求方法
     */
    public static ResponseResult httpRequest(String urlStr, String method, String cookie, String postData, Map<String, String> headers, int timeoutSeconds) {
        HttpURLConnection conn = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeoutSeconds * 1000);
            conn.setReadTimeout(timeoutSeconds * 1000);
            conn.setRequestMethod(method);
            conn.setInstanceFollowRedirects(false);

            // 仿 SummerSec 头
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Connection", "close");

            if (cookie != null && !cookie.isEmpty()) {
                conn.setRequestProperty("Cookie", "rememberMe=" + cookie);
            }

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            if ("POST".equalsIgnoreCase(method)) {
                conn.setDoOutput(true);
                if (headers == null || !headers.containsKey("Content-Type")) {
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                }
                if (postData != null) {
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(postData.getBytes(StandardCharsets.UTF_8));
                        os.flush();
                    }
                }
            }

            conn.connect();

            // === 核心修改：SummerSec 逻辑 ===
            int code = conn.getResponseCode();
            String body = "";

            // 尝试从正常流读取
            try {
                body = readBytes(conn.getInputStream());
            } catch (IOException e) {
                if (body.isEmpty()) {
                    body = readBytes(conn.getErrorStream());
                }
            }

            // 获取 deleteMe 数量
            Map<String, List<String>> respHeaders = conn.getHeaderFields();
            int deleteMeCount = 0;
            if (respHeaders.get("Set-Cookie") != null) {
                for (String c : respHeaders.get("Set-Cookie")) {
                    if (c.contains("deleteMe")) deleteMeCount++;
                }
            }

            return new ResponseResult(code, body, deleteMeCount);

        } catch (Exception e) {
            return new ResponseResult(0, "NetError: " + e.getMessage(), 0);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * 读到报错为止，返回已读到的内容
     */
    private static String readBytes(InputStream is) {
        if (is == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {

        }
        return new String(baos.toByteArray(), StandardCharsets.UTF_8);
    }

    public static class ResponseResult {
        public int code;
        public String body;
        public int deleteMeCount;

        public ResponseResult(int code, String body, int deleteMeCount) {
            this.code = code;
            this.body = body;
            this.deleteMeCount = deleteMeCount;
        }
    }
}