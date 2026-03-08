package com.navi.service;

import com.navi.dto.ConsentRevokeRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class ConsentRevocationService {
    private final RestClient consentRestClient;

    public ConsentRevocationService(@Qualifier("bankConsentRestClient") RestClient consentRestClient) {
        this.consentRestClient = consentRestClient;
    }

    public void revokeConsent(String consentId, String tokenType) {
        ConsentRevokeRequest body = new ConsentRevokeRequest();
        body.setReasonCode("TPP_REQUEST");
        body.setReasonDetail("User revoked on TPP side");
        body.setRevokeMeta(Map.of("tokenType", tokenType)); // ví dụ "refresh_token"

        consentRestClient.post()
                .uri("/internal/v1/ais/consents/{consentId}/revoked", consentId)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
