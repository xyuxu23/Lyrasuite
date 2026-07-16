package com.xyuxu.javasec.dto;

import com.xyuxu.javasec.core.payload.jndi.JNDIType;
import lombok.Data;

@Data
public class JndiRequest {

    private String ip;
    private JNDIType type;
    private String bypassType;
    private String gadget;
    private String command;

}