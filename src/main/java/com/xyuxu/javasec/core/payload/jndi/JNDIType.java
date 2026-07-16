package com.xyuxu.javasec.core.payload.jndi;

public enum JNDIType {

    LDAP_SERIALIZED("LDAP Serialized Data"),
    RMI_LOCAL_FACTORY("RMI Local Factory"),
    REMOTE_REFERENCE("Remote Reference (JDK < 8u191)");

    private final String desc;
    JNDIType(String desc) { this.desc = desc; }
    @Override public String toString() { return desc; }
}