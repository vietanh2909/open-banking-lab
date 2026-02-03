package com.navi.controller;


import com.navi.dto.TokenExchangeRequest;
import com.navi.dto.TokenResponse;
import com.navi.service.CiamTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/ciam")
public class AuthController {

    private final CiamTokenService ciamTokenService;

    public AuthController(CiamTokenService ciamTokenService) {
        this.ciamTokenService = ciamTokenService;
    }

    // FE gọi endpoint này ở /oidc/callback
    @PostMapping("/callback")
    public TokenResponse callback(@Valid @RequestBody TokenExchangeRequest request) {
        return ciamTokenService.exchange(request);
    }

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true);
    }
}