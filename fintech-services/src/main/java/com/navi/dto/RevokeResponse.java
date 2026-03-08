package com.navi.dto;


import lombok.Data;

import java.time.Instant;

@Data
public class RevokeResponse {

    private String subject;
    private String consentId;
    private boolean linked;
    private boolean ciamRevoked;
    private boolean consentRevoked;
    private Instant revokedAt;
}