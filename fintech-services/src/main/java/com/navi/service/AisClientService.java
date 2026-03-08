package com.navi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navi.domain.AccessTokenEntity;
import com.navi.dto.AccountInfoRequest;
import com.navi.dto.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

@Service
public class AisClientService {

    private final RestClient bankAisRestClient;
    private final TokenStoreService tokenStoreService;

    private static final Logger LOG = LoggerFactory.getLogger(AisClientService.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public AisClientService(RestClient bankAisRestClient, TokenStoreService tokenStoreService) {
        this.bankAisRestClient = bankAisRestClient;
        this.tokenStoreService = tokenStoreService;
    }

    public Map<?, ?> getAccounts(String subject) {
        AccessTokenEntity accessToken = tokenStoreService.getValidAccessTokenOrThrow(subject);

        return bankAisRestClient.get()
                .uri("/api/v1/accounts")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken())
                .header("X-Consent-Id",accessToken.getConsentId())
                .retrieve()
                .body(Map.class);
    }

    public Map<?, ?> getAccountDetail(String subject, String accountId) {

        try {
            AccessTokenEntity accessToken = tokenStoreService.getValidAccessTokenOrThrow(subject);

            AccountInfoRequest request = new AccountInfoRequest();
            request.setAccountId(accountId);

            return bankAisRestClient.post()
                    .uri("/api/v1/accounts/information")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken())
                    .header("X-Consent-Id",accessToken.getConsentId())
                    .body(request)
                    .retrieve()
                    .body(Map.class);
        } catch (Exception ex) {
            LOG.error("GET ACCOUNT DETAIL ERROR " + ex.getMessage());
            return null;
        }

    }

    public Map<?, ?> getTransactions(String subject, String accountId, String fromDate, String toDate, Integer page, Integer size) {
        AccessTokenEntity accessToken = tokenStoreService.getValidAccessTokenOrThrow(subject);

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(accountId);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setPage(page);
        request.setSize(size);

        return bankAisRestClient.post()
                .uri("/api/v1/accounts/transactions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.getAccessToken())
                .header("X-Consent-Id",accessToken.getConsentId())
                .body(request)
                .retrieve()
                .body(Map.class);
    }
}