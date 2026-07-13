package com.aianik.anik.ai.features.rag.util;

import com.aianik.anik.ai.common.execption.AnikAiException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class ContentHashUtil {

    private ContentHashUtil() {
    }

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder sb = new StringBuilder(64);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new AnikAiException("SHA-256 algorithm not available", e);
        }
    }

    public static String sha256Hex(String text) {
        if (text == null) {
            return null;
        }
        return sha256Hex(text.getBytes(StandardCharsets.UTF_8));
    }
}
