package com.navi.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
@Validated
@ConfigurationProperties(prefix = "fintech.ciam")
@Data
public class CIAMProperties {
    @NotBlank
    private String issuer;

    @NotBlank
    private String clientId;

    private String clientSecret;

    /** Optional override. If empty -> issuer + "/protocol/openid-connect/token" */
    private String tokenEndpoint;

    private String revokeTokenEndpoint;

    public String resolvedTokenEndpoint() {
        //if (tokenEndpoint != null && !tokenEndpoint.isBlank()) return tokenEndpoint;
        String base = issuer.endsWith("/") ? issuer.substring(0, issuer.length() - 1) : issuer;
        return base + tokenEndpoint;
    }

    public String resolvedRevokeEndpoint() {
        //if (revokeTokenEndpoint != null && !revokeTokenEndpoint.isBlank()) return revokeTokenEndpoint;
        String base = issuer.endsWith("/") ? issuer.substring(0, issuer.length() - 1) : issuer;
        return base + revokeTokenEndpoint;
    }

    public boolean hasSecret() {
        return clientSecret != null && !clientSecret.isBlank();
    }
}
