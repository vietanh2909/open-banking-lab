package com.navi.ob.ewlts.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class CryptoUtil {
    private CryptoUtil() {}
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}