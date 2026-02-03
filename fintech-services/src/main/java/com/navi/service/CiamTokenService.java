package com.navi.service;


import com.navi.config.CIAMProperties;
import com.navi.dto.TokenExchangeRequest;
import com.navi.dto.TokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

@Service
public class CiamTokenService {

    private final RestClient restClient;
    private final CIAMProperties props;

    public CiamTokenService(RestClient restClient, CIAMProperties props) {
        this.restClient = restClient;
        this.props = props;
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
            return restClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(form)
                    .retrieve()
                    .body(TokenResponse.class);


        } catch (HttpStatusCodeException e) {
            // trả lỗi rõ ràng cho FE debug
            throw new RuntimeException("CIAM token exchange failed: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        }
    }
}