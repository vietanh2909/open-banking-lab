package com.navi.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
@Validated
@ConfigurationProperties(prefix = "fintech.ciam")
public class CIAMProperties {
    @NotBlank
    private String issuer;

    @NotBlank
    private String clientId;

    private String clientSecret;

    /** Optional override. If empty -> issuer + "/protocol/openid-connect/token" */
    private String tokenEndpoint;

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getTokenEndpoint() { return tokenEndpoint; }
    public void setTokenEndpoint(String tokenEndpoint) { this.tokenEndpoint = tokenEndpoint; }

    public String resolvedTokenEndpoint() {
        if (tokenEndpoint != null && !tokenEndpoint.isBlank()) return tokenEndpoint;
        String base = issuer.endsWith("/") ? issuer.substring(0, issuer.length() - 1) : issuer;
        return base + "/protocol/openid-connect/token";
    }

    public boolean hasSecret() {
        return clientSecret != null && !clientSecret.isBlank();
    }
}
