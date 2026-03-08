package com.navi.ob.security.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ConsentVerifyClient {

    private final RestClient rest;

    public ConsentVerifyClient(RestClient.Builder builder,
                               ConsentVerifyProperties props) {
        this.rest = builder
                .baseUrl(props.getBaseUrl())
                .build();
    }

    /**
     * Fail-closed: consent-service lỗi/timeout => deny (return false)
     */
    public boolean verify(String consentId, String authorization) {
        try {
            HttpStatusCode status = rest.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/internal/consents/verify")
                            .build())
                    .header("X-Consent-Id", consentId)
                    .header("Authorization", authorization)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            return status.is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}