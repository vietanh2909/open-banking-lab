package com.navi.dto;

import lombok.Data;

@Data
public class TokenResponse {
    private String access_token;
    private String refresh_token;
    private String id_token;
    private Long expires_in;
    private String token_type;
    private String scope;
}
