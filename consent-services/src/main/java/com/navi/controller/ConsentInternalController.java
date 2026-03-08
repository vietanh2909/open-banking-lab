package com.navi.controller;

import com.navi.dto.VerifyConsentResponse;
import com.navi.service.ConsentVerifyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/consents")
public class ConsentInternalController {

    private final ConsentVerifyService verifyService;

    public ConsentInternalController(ConsentVerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @PostMapping("/verify")
    public VerifyConsentResponse verify(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader("X-Consent-Id") String consentId
    ) {
        return verifyService.verify(jwt, consentId);
    }
}
