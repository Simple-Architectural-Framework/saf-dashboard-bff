package com.ican.saf.ui.dsahboard.bff.crypto;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

/**
 * Implementation of {@link StringEncryptorInt} that uses the DESede (Triple DES) algorithm for encryption and decryption.
 * This class is intended to be used as a Spring bean and expects the encryption scheme to be configured
 * via the {@code crypto.desede_encryption_scheme} property.
 */
public class StringEncryptorDesede implements StringEncryptorInt{
    @Value("${crypto.desede_encryption_scheme}")
    private String desedeEncryptionScheme;

    private SecretKeyFactory secretKeyFactory;
    private Cipher cipher;
    private KeySpec keySpec;

    /**
     * Encrypts the given unencrypted string using the provided encryption key.
     * The encryption key must be at least 24 characters long for DESede.
     *
     * @param unencryptedString the string to be encrypted. Must not be null or empty.
     * @param encryptionKey     the key to be used for encryption. Must not be null and must be at least 24 characters long.
     * @return the Base64 encoded encrypted string.
     * @throws EncryptionException      if an error occurs during the encryption process.
     * @throws IllegalArgumentException if the unencrypted string is null or empty, or if the encryption key is null or too short.
     */
    @Override
    public String encrypt(String unencryptedString, String encryptionKey) throws EncryptionException {
        if(unencryptedString == null || unencryptedString.trim().isEmpty())
            throw new IllegalArgumentException("unencrypted string was null or empty");

        if(encryptionKey == null)
            throw new IllegalArgumentException("encryption key null");

        if(encryptionKey.trim().length() < 24)
            throw new IllegalArgumentException("encryption key was less than 24 characters");

        try {
            byte[] keyAsBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            keySpec = new DESedeKeySpec(keyAsBytes);
        } catch (InvalidKeyException e) {
            throw new EncryptionException(e);
        }
        return enc(cipher, keySpec, secretKeyFactory, unencryptedString);
    }

    /**
     * Decrypts the given Base64 encoded encrypted string.
     * Note: This method uses the {@link KeySpec} that was initialized during the last call to {@link #encrypt(String, String)}.
     *
     * @param encryptedString the Base64 encoded encrypted string to be decrypted. Must not be null or empty.
     * @return the decrypted string.
     * @throws EncryptionException      if an error occurs during the decryption process.
     * @throws IllegalArgumentException if the encrypted string is null or empty.
     */
    @Override
    public String decrypt(String encryptedString) throws EncryptionException {
        if(encryptedString == null || encryptedString.trim().isEmpty())
            throw new IllegalArgumentException("encrypted word is empty");
        return dec(cipher, keySpec, secretKeyFactory, encryptedString);
    }

    /**
     * Initializes the {@link SecretKeyFactory} and {@link Cipher} instances based on the configured encryption scheme.
     * This method is annotated with {@link PostConstruct} and will be called after the bean is constructed.
     *
     * @throws EncryptionException if the encryption scheme is invalid or not supported.
     */
    @Override
    @PostConstruct
    public void initialize() throws EncryptionException {
        try {
            secretKeyFactory = SecretKeyFactory.getInstance(desedeEncryptionScheme);
            cipher = Cipher.getInstance(desedeEncryptionScheme);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e){
            throw new EncryptionException(e);
        }
    }
}
