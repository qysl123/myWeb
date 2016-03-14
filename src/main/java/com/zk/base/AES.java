/**
 * 
 */
package com.zk.base;

import jodd.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

/**
 * @author <a href="mailto:xuyy@yichenghome.com">Xu Yuanyuan</a>
 * @version 1.0
 * @date 2016年1月20日 上午11:01:17
 * @desc
 */
public class AES {

    public static String DES              = "AES";
    public static String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    public static Key getSecretKey(String key) throws Exception {
        SecretKey securekey = null;
        if (key == null) {
            key = "";
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance(DES);
        keyGenerator.init(128);
        securekey = keyGenerator.generateKey();
        return securekey;
    }

    public static String encrypt(String data, String key) throws Exception {
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(),"AES");
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, skey);
        byte[] crypted = cipher.doFinal(data.getBytes());
        return Base64.encodeToString(crypted);
    }

    public static String detrypt(String message, String key) throws Exception {
        byte[] output = null;
        SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, skey);
        output = cipher.doFinal(Base64.decode(message));
        return new String(output);
    }
}