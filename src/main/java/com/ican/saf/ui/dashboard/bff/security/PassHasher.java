package com.ican.saf.ui.dsahboard.bff.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public class PassHasher {
    public static SaltAndHash hash(String password) throws NoSuchAlgorithmException {
        //1. Generate a random Salt using secure random
        byte[] salt = new byte[16];
        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        //2. Hash the password using SHA-256 and the salt
        var md = MessageDigest.getInstance("SHA-256");

        //3. Add salt to the digest
        md.update(salt);

        //4. Hash the password again and return the result
        byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        //5. Convert to Hex strings for storage
        var saltAndHash = new SaltAndHash();
        saltAndHash.setSaltHex(HexFormat.of().formatHex(salt));
        saltAndHash.setHashHex(HexFormat.of().formatHex(hashedBytes));
        return saltAndHash;
    }
}
