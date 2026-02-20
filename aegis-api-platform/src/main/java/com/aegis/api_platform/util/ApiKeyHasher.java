package com.aegis.api_platform.util;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyHasher {

    public String hash(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash API key", e);
        }
    }
}
