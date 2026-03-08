package com.navi.service;

import com.navi.config.CIAMProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class CiamRevocationService {
    private final RestClient restClient;

    private final CIAMProperties props;

    public CiamRevocationService(RestClient restClient, CIAMProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public void revokeToken(String token, String tokenTypeHint) {
        if (token == null || token.isBlank()) return;
        String revokeTokenUrl = props.resolvedRevokeEndpoint();
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", props.getClientId());
        form.add("client_secret", props.getClientSecret());

        form.add("token", token);
        if (tokenTypeHint != null && !tokenTypeHint.isBlank()) {
            form.add("token_type_hint", tokenTypeHint); // "refresh_token" / "access_token"
        }

        // Keycloak trả 200/204 là ok. Sai token cũng có thể trả 200 theo spec.
        restClient.post()
                .uri(revokeTokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
    }
}
