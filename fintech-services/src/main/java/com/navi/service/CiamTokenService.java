package com.navi.service;


import com.navi.config.CIAMProperties;
import com.navi.dto.TokenExchangeRequest;
import com.navi.dto.TokenResponse;
import com.navi.utils.JwtClaimExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@Service
public class CiamTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(CiamTokenService.class);
    private final RestClient restClient;
    private final CIAMProperties props;

    private final TokenStoreService tokenStoreService;

    public CiamTokenService(RestClient restClient, CIAMProperties props, TokenStoreService tokenStoreService) {
        this.restClient = restClient;
        this.props = props;
        this.tokenStoreService = tokenStoreService;
    }

    public TokenResponse exchange(TokenExchangeRequest req) {
        String tokenUrl = props.resolvedTokenEndpoint();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", props.getClientId());
        form.add("code", req.getCode());
        form.add("redirect_uri", req.getRedirectUri());
        form.add("code_verifier", req.getCodeVerifier());

        // Nếu client confidential
        if (props.hasSecret()) {
            form.add("client_secret", props.getClientSecret());
        }

        try {
            TokenResponse resp = restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);

            String subject = "navitagi"; // ví dụ

            if (resp != null) {
                LOG.info("exchange|Access Token: " + resp.getAccess_token());

                String consentId = JwtClaimExtractor.getStringClaim(resp.getAccess_token(), "consent_id");

                LOG.info("exchange|consent_id (from access_token): " + consentId);

                Instant expiresAt = Instant.now().plusSeconds(resp.getExpires_in());
                tokenStoreService.saveToken(
                        subject,
                        resp.getScope() != null ? resp.getScope() : null,
                        resp.getAccess_token(),
                        expiresAt,
                        resp.getRefresh_token(),
                        consentId
                );
            }

            return resp;

        } catch (HttpStatusCodeException e) {
            // trả lỗi rõ ràng cho FE debug
            throw new RuntimeException("CIAM token exchange failed: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        }
    }
}