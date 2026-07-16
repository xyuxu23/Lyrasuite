package com.xyuxu.javasec.dto;


import lombok.Data;

@Data
public class GadgetRequest {

    private String gadget;
    private String command;
    private String encoding;
    private boolean dirtyData;

}