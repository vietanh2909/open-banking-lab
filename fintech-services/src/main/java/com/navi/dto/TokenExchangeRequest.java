package com.navi.dto;

import jakarta.validation.constraints.NotBlank;

public class TokenExchangeRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String codeVerifier;

    @NotBlank
    private String redirectUri;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCodeVerifier() { return codeVerifier; }
    public void setCodeVerifier(String codeVerifier) { this.codeVerifier = codeVerifier; }

    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}
