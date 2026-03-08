package com.navi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
