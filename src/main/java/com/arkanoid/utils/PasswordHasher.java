package com.arkanoid.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for password hashing and verification using SHA-256 with salt.
 */
public class PasswordHasher {
    private static final String ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SALT_LENGTH = 16;

    /**
     * Hash a password with a generated salt
     * 
     * @param password plain text password
     * @return hashed password with salt (format: salt:hash)
     */
    public String hashPassword(String password) {
        byte[] salt = generateSalt();
        String hash = hashWithSalt(password, salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + hash;
    }

    /**
     * Verify a password against a stored hash
     * 
     * @param password   plain text password to verify
     * @param hashedPassword stored hash (format: salt:hash)
     * @return true if password matches
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        String[] parts = hashedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String expectedHash = parts[1];
        String actualHash = hashWithSalt(password, salt);

        return expectedHash.equals(actualHash);
    }

    private byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return salt;
    }

    private String hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
