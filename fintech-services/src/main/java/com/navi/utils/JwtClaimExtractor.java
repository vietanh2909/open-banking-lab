package com.navi.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;

public final class JwtClaimExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JwtClaimExtractor() {}

    public static String getStringClaim(String jwt, String claimName) {
        if (jwt == null || jwt.isBlank()) return null;

        String[] parts = jwt.split("\\.");
        if (parts.length < 2) return null; // not a JWT

        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        try {
            JsonNode node = MAPPER.readTree(payloadJson);
            JsonNode claim = node.get(claimName);
            return (claim == null || claim.isNull()) ? null : claim.asText();
        } catch (Exception e) {
            return null;
        }
    }
}