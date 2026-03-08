package com.navi.ob.security.config;

import com.navi.ob.security.ConsentAuthorizationManager;
import com.navi.ob.security.client.ConsentVerifyClient;
import com.navi.ob.security.client.ConsentVerifyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableConfigurationProperties(ConsentVerifyProperties.class)
public class SecurityConfig {


    @Bean
    AuthorizationManager<RequestAuthorizationContext> consentAuthorizationManager(
            ConsentVerifyClient client
    ) {
        return new ConsentAuthorizationManager(client);
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    AuthorizationManager<RequestAuthorizationContext> consentAuthorizationManager) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // chỉ check consent cho nhóm AIS endpoints bạn yêu cầu
                        .requestMatchers("/api/v1/accounts", "/api/v1/accounts/information", "/api/v1/accounts/transactions")
                        .access(consentAuthorizationManager)

                        // các API khác không authen
                        .anyRequest().permitAll()
                )
                // OPTIONAL nhưng khuyến nghị: validate JWT locally (signature/exp) trước khi gọi consent
                // Nếu bạn muốn banking-service KHÔNG validate local, có thể bỏ hẳn oauth2ResourceServer.
                //.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }
}