package com.xyuxu.javasec.utils;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public final class Reflections {

    private static Unsafe unsafe;

    static {
        try {
            // 获取 Unsafe 实例
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
        } catch (Exception e) {

        }
    }

    private Reflections() {
    }

    /**
     * 使用 Unsafe 强制设置字段值
     */
    public static void setFieldValueUnsafe(final Object obj, final String fieldName, final Object value) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        if (unsafe != null) {
            long offset = unsafe.objectFieldOffset(field);
            unsafe.putObject(obj, offset, value);
        } else {
            // 回退到普通反射
            field.set(obj, value);
        }
    }

    /**
     * 通过反射调用构造函数
     */
    public static <T> T createInstance(final String className, final Class<?>[] parameterTypes, final Object[] args) throws Exception {
        final Class<?> clazz = Class.forName(className);
        final Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
        constructor.setAccessible(true);
        return (T) constructor.newInstance(args);
    }

    /**
     * 普通反射设置字段值
     */
    public static void setFieldValue(final Object obj, final String fieldName, final Object value) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        field.set(obj, value);
    }

    /**
     * 获取字段值
     */
    public static Object getFieldValue(final Object obj, final String fieldName) throws Exception {
        final Field field = getField(obj.getClass(), fieldName);
        return field.get(obj);
    }

    /**
     * 递归获取字段对象
     */
    private static Field getField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                final Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException("Field '" + fieldName + "' not found in class hierarchy of " + clazz.getName());
    }

    /**
     * 绕过构造函数实例化类
     */
    public static <T> T createWithoutConstructor(Class<T> classToInstantiate) throws Exception {
        ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
        Constructor<?> constructor = Object.class.getDeclaredConstructor();
        Constructor<?> newConstructor = reflectionFactory.newConstructorForSerialization(classToInstantiate, constructor);
        newConstructor.setAccessible(true);
        return (T) newConstructor.newInstance();
    }




}