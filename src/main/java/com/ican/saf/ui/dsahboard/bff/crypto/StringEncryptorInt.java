package com.ican.saf.ui.dsahboard.bff.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public interface StringEncryptorInt {
    Logger logger = LoggerFactory.getLogger(StringEncryptorInt.class);
    String encrypt(String unencryptedString, String encryptionKey) throws EncryptionException;
    String decrypt(String encryptedString) throws EncryptionException;
    void initialize() throws EncryptionException;

    default String enc(Cipher cipher, KeySpec keySpec, SecretKeyFactory keyFactory, String unencryptedString)
    throws EncryptionException {
        try {
            var key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            var clearText = unencryptedString.getBytes(StandardCharsets.UTF_8);
            var cipherText = cipher.doFinal(clearText);

            return Base64.getEncoder().encodeToString(cipherText);

        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    default String dec(Cipher cipher, KeySpec keySpec, SecretKeyFactory keyFactory, String encryptedString)
        throws EncryptionException {
        try {
            var key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key);

            var clearText = Base64.getDecoder().decode(encryptedString);
            var cipherText = cipher.doFinal(clearText);

            return bytes2String(cipherText);

        } catch (Exception e){
            throw new EncryptionException(e);
        }
    }



    static String bytes2String(byte[] bytes){
        var stringBuilder = new StringBuilder();
        for(var i : bytes){
            stringBuilder.append((char)i);
        }
        return stringBuilder.toString();
    }

    class EncryptionException extends Exception{
        public EncryptionException(Throwable t){super(t);}
    }
}
