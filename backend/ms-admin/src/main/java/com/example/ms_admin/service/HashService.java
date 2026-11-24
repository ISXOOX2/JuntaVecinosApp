package com.example.ms_admin.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;

// Hash SHA-1 igual que el ejemplo del profesor.
// En produccion se usaria BCrypt (mas lento y con salt), no SHA-1.
@Service
public class HashService {

    public String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("Unable to compute SHA-1", e);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
