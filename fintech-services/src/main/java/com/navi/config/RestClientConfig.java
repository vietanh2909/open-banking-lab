package com.navi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CIAMProperties.class)
public class RestClientConfig {

    @Value("${banking.base-url}")
    private String bankingBaseUrl;

    @Value("${banking.consent-url}")
    private String bankingConsentUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }

    @Bean
    public RestClient bankAisRestClient() {
        return RestClient.builder()
                .baseUrl(bankingBaseUrl)
                .build();
    }

    @Bean
    public RestClient bankConsentRestClient() {
        return RestClient.builder()
                .baseUrl(bankingConsentUrl)
                .build();
    }
}
