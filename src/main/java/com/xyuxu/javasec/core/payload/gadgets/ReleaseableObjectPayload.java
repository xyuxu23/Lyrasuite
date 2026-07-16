package com.xyuxu.javasec.core.payload.gadgets;

public interface ReleaseableObjectPayload<T> extends ObjectPayload<T> {

    void release( T object ) throws Exception;

}
