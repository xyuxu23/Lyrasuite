package com.xyuxu.javasec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class LyraWebApplication {
    static {
        System.setProperty("sun.rmi.registry.registryFilter", "*");
        System.setProperty("org.apache.commons.collections.enableUnsafeSerialization", "true");
    }

    public static void main(String[] args) {
        SpringApplication.run(LyraWebApplication.class, args);
    }

    /**
     * 动态获取端口并打印启动 Banner
     */
    @EventListener(ApplicationReadyEvent.class)
    public void printBanner(ApplicationReadyEvent event) {
        String port = event.getApplicationContext().getEnvironment().getProperty("local.server.port", "8080");
        final String PURPLE = "\033[0;35m";
        final String GREEN = "\033[0;32m";
        final String RESET = "\033[0m";
        String banner = PURPLE +
                "         ,           \n" +
                "      \\  :  /        \n" +
                "    `. `.:.' .'      \n" +
                "  --  .     .  --    \n" +
                "    .' ..:.. `.      \n" +
                "   /  .  :  .  \\     \n" +
                "\n" +
                "   LyraSuite Web Mode Started!    \n" +
                RESET;

        System.out.println(banner);
        System.out.println(GREEN + "[+] Server running at: http://localhost:" + port + RESET);
    }
}