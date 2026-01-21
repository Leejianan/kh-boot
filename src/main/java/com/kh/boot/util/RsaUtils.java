package com.kh.boot.util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Utility for RSA encryption and decryption.
 */
public class RsaUtils {

    /**
     * Decrypt data using RSA private key.
     *
     * @param encryptedData    Base64 encoded encrypted data
     * @param privateKeyBase64 Base64 encoded private key (PKCS#8)
     * @return Decrypted string
     * @throws Exception if decryption fails
     */
    public static String decrypt(String encryptedData, String privateKeyBase64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Use standard PKCS#1 v1.5 padding to match JSEncrypt and common RSA usage
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}
