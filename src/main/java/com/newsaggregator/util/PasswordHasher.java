package com.newsaggregator.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for hashing and verifying passwords.
 */
public class PasswordHasher {
    private static final Logger LOGGER = Logger.getLogger(PasswordHasher.class.getName());
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final String DELIMITER = ":";

    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordHasher() {
    }

    /**
     * Hashes a password with a random salt.
     *
     * @param password the password to hash
     * @return the hashed password with salt
     */
    public static String hashPassword(String password) {
        try {
            // Generate a random salt
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Create MessageDigest instance for SHA-256
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            
            // Add salt to digest
            md.update(salt);
            
            // Get the hashed password
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Store salt and hashed password
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            // Return salt:hash
            return saltBase64 + DELIMITER + hashBase64;
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password       the password to verify
     * @param storedPassword the stored password hash
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedPassword) {
        try {
            // Split the stored password into salt and hash
            String[] parts = storedPassword.split(DELIMITER);
            if (parts.length != 2) {
                LOGGER.warning("Invalid stored password format");
                return false;
            }
            
            // Decode the salt and hash
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the given password with the same salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] newHash = md.digest(password.getBytes());
            
            // Compare the hashes
            if (hash.length != newHash.length) {
                return false;
            }
            
            for (int i = 0; i < hash.length; i++) {
                if (hash[i] != newHash[i]) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying password", e);
            return false;
        }
    }
}
