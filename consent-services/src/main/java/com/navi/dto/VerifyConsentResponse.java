package com.navi.dto;

import lombok.Data;

@Data
public class VerifyConsentResponse {
    boolean valid;
    String consentId;
    String consentType;
    String status;
    String clientId;
    String providerId;
    String psuId;
    String scope;
}
