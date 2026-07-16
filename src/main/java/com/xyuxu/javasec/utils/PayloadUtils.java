package com.xyuxu.javasec.utils;

import com.xyuxu.javasec.core.payload.gadgets.BytecodePayload;
import com.xyuxu.javasec.core.payload.gadgets.ObjectPayload;
import com.xyuxu.javasec.core.payload.gadgets.ReleaseableObjectPayload;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PayloadUtils {

    /**
     * 缓存所有 ObjectPayload
     */
    private static final Map<String, Class<? extends ObjectPayload>> PAYLOAD_CACHE = new ConcurrentHashMap<>();

    static {
        final Reflections reflections = new Reflections(ObjectPayload.class.getPackage().getName());
        final Set<Class<? extends ObjectPayload>> payloadTypes = reflections.getSubTypesOf(ObjectPayload.class);

        for (Class<? extends ObjectPayload> pc : payloadTypes) {
            if (pc.isInterface() || Modifier.isAbstract(pc.getModifiers())) {
                continue;
            }
            PAYLOAD_CACHE.put(pc.getSimpleName(), pc);
        }
    }

    /**
     * 获取所有 Payload 类
     */
    public static Set<Class<? extends ObjectPayload>> getAllPayloadClasses() {
        return new HashSet<>(PAYLOAD_CACHE.values());
    }


    /**
     * 获取所有支持字节码注入的 Payload
     */
    @SuppressWarnings("unchecked")
    public static Set<Class<? extends BytecodePayload>> getBytecodePayloadClasses() {
        return PAYLOAD_CACHE.values().stream()
                .filter(BytecodePayload.class::isAssignableFrom)
                .map(clazz -> (Class<? extends BytecodePayload>) clazz)
                .collect(Collectors.toSet());
    }


    /**
     * 根据类名获取 Class
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends ObjectPayload> getPayloadClass(final String className) {
        Class<? extends ObjectPayload> clazz = PAYLOAD_CACHE.get(className);
        if (clazz != null) {
            return clazz;
        }
        try {
            Class<?> loadedClass = Class.forName(className);
            if (ObjectPayload.class.isAssignableFrom(loadedClass)) {
                clazz = (Class<? extends ObjectPayload>) loadedClass;
                PAYLOAD_CACHE.put(clazz.getSimpleName(), clazz);
                return clazz;
            }
        } catch (ClassNotFoundException e) {
        }
        return null;
    }

    /**
     * 实例化 Payload
     */
    public static Object makePayloadObject(String payloadType, String payloadArg) {
        final Class<? extends ObjectPayload> payloadClass = getPayloadClass(payloadType);
        if (payloadClass == null) {
            throw new RuntimeException("Payload class not found: " + payloadType);
        }
        try {
            final ObjectPayload<?> payload = payloadClass.getDeclaredConstructor().newInstance();
            return payload.getObject(payloadArg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct payload: " + payloadType, e);
        }
    }

    public static void releasePayload(Object payload, Object object) throws Exception {
        if (payload instanceof ReleaseableObjectPayload) {
            ((ReleaseableObjectPayload) payload).release(object);
        }
    }
}