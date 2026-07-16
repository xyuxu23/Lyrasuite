package com.xyuxu.javasec.core.payload.gadgets;

public interface BytecodePayload<T> {
    /**
     * 接收字节码并生成利用对象
     * @param bytecode 恶意的 .class 字节码 (如 TomcatEcho)
     * @return 序列化前的对象 (如 PriorityQueue)
     */
    T getObjectWithBytecode(byte[] bytecode) throws Exception;


}