package com.navi.ob.security;

import com.navi.ob.security.client.ConsentVerifyClient;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

public class ConsentAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final ConsentVerifyClient client;

    public ConsentAuthorizationManager(ConsentVerifyClient client) {
        this.client = client;
    }

    @Override
    public AuthorizationDecision check(java.util.function.Supplier<org.springframework.security.core.Authentication> authentication,
                                       RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();

        String consentId = request.getHeader("X-Consent-Id");
        String authorization = request.getHeader("Authorization");

        if (consentId == null || consentId.isBlank()) {
            return new AuthorizationDecision(false);
        }
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return new AuthorizationDecision(false);
        }

        boolean ok = client.verify(consentId, authorization);
        return new AuthorizationDecision(ok);
    }
}
