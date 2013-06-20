package com.ht.scada.communication.util.encryption;

import com.ht.scada.communication.util.base64.Base64Utils;

public class AESTester {
    
    static String key;
    
    static {
        try {
            key = AESUtils.getSecretKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(key);
        System.out.println(AESUtils.getSecretKey());
        System.out.println(AESUtils.getSecretKey("abc"));
        System.out.println(AESUtils.getSecretKey("abc"));
        long begin = System.currentTimeMillis();
        //encryptFile();
        //decryptFile();
        test();
        long end = System.currentTimeMillis();
        System.err.println("耗时：" + (end-begin)/1000 + "秒");
    }
    
    static void encryptFile() throws Exception {
        String sourceFilePath = "D:/demo.mp4";
        String destFilePath = "D:/demo_encrypted.mp4";
        AESUtils.encryptFile(key, sourceFilePath, destFilePath);
    }
    
    static void decryptFile() throws Exception {
        String sourceFilePath = "D:/demo_encrypted.mp4";
        String destFilePath = "D:/demo_decrypted.mp4";
        AESUtils.decryptFile(key, sourceFilePath, destFilePath);
    }
    
    static void test() throws Exception {
        //String source = "这是一行测试AES加密/解密的文字，你看完也等于没看，是不是啊？！";
        String source = "abcdefghijklmnopqrstuvwxyz0123456789";
        System.out.println("原文:\t" + source);
        System.out.println("密钥:\t" + key);
        byte[] inputData = source.getBytes();
        inputData = AESUtils.encrypt(inputData, key);
        System.out.println("加密后:\t" + Base64Utils.encode(inputData));
        byte[] outputData = AESUtils.decrypt(inputData, key);
        String outputStr = new String(outputData);
        System.out.println("解密后:\t" + outputStr);
    }

}