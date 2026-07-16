package com.xyuxu.javasec.dto;

import lombok.Data;

@Data
public class ShiroRequest {
    private String url;
    private String method;
    private String postData;
    private int timeout;
    private String specifiedKey;
    private String specifiedGadget;
    private String specifiedEcho;
    private String command;
    private boolean isGcm;
    private boolean checkGadget = true;
    private boolean preloadEcho;

}