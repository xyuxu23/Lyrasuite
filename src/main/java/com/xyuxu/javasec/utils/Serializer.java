package com.xyuxu.javasec.utils;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class Serializer implements Callable<byte[]> {

    private final Object object;

    public Serializer(Object object) {this.object = object;}

    @Override
    public byte[] call() throws Exception {
        return serialize(object);
    }


    public static byte[] serialize(final Object obj) throws Exception{
        final ByteArrayOutputStream out =new ByteArrayOutputStream();
        serialize(obj,out);
        return out.toByteArray();
    }

    public static void serialize(final Object obj, final OutputStream out) throws Exception{

        final ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(obj);

    }

    public static void hackSerialVersionUID(Class<?> clazz, long uid) {
        try {
            // 1. 获取该类的 ObjectStreamClass 描述符
            ObjectStreamClass osc = ObjectStreamClass.lookup(clazz);

            // 2. 反射获取 ObjectStreamClass 内部的 "suid" 字段 (这是存 UID 的地方)
            Field suidField = ObjectStreamClass.class.getDeclaredField("suid");
            suidField.setAccessible(true);

            // 3. 强制修改缓存中的 UID
            suidField.set(osc, Long.valueOf(uid));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
