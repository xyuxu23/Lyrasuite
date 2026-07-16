package com.xyuxu.javasec.dto;

import lombok.Data;

@Data
public class SnakeYamlRequest {
    private String ip;
    private String type;
    private String jndiUrl;
    private String command;
}