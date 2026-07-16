package com.xyuxu.javasec.dto;

import lombok.Data;

@Data
public class FastjsonRequest {

    private String type;
    private String command;
    private String jndiUrl;
    private String echoType;
}