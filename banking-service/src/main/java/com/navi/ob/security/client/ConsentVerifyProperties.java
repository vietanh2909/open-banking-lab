package com.navi.ob.security.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "consent.verify")
public class ConsentVerifyProperties {
    /**
     * e.g. http://consent-service:8082
     */
    private String baseUrl = "http://consent-service:8082";

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}