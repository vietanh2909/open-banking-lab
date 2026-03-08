package com.navi.service;
import com.navi.domain.AccessTokenEntity;
import com.navi.dto.RevokeResponse;
import com.navi.repository.AccessTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RevokeService {
    private final AccessTokenRepository tokenRepo;
    private final CiamRevocationService ciamRevocationService;
    private final ConsentRevocationService consentRevocationService;

    public RevokeService(AccessTokenRepository tokenRepo,
                             CiamRevocationService ciamRevocationService,
                             ConsentRevocationService consentRevocationService) {
        this.tokenRepo = tokenRepo;
        this.ciamRevocationService = ciamRevocationService;
        this.consentRevocationService = consentRevocationService;
    }

    @Transactional
    public RevokeResponse revokeEverywhere(String subject) {
        AccessTokenEntity e = tokenRepo.findFirstBySubjectAndActiveTrueOrderByCreatedAtDesc(subject)
                .orElseThrow(() -> new IllegalStateException("No active token for subject=" + subject));

        boolean ciamRevoked = false;
        boolean consentRevoked = false;
        String consentId = String.valueOf(e.getConsentId());

        // 1) revoke ở CIAM (ưu tiên refresh_token (AIS), rồi access_token(PIS))
        if (e.getRefreshToken() != null) {
            ciamRevocationService.revokeToken(e.getRefreshToken(), "refresh_token");
            ciamRevoked = true;
        } else {
            // fallback nếu không có refresh_token
            ciamRevocationService.revokeToken(e.getAccessToken(), "access_token");
            ciamRevoked = true;
        }

        // 2) revoke ở Consent Management (bạn yêu cầu tokenType=refresh_token)
        if (consentId != null && !consentId.isBlank()) {
            consentRevocationService.revokeConsent(consentId, "refresh_token");
            consentRevoked = true;
        }

        // 3) đánh dấu local DB inactive
        e.setActive(false);
        e.setRevokedAt(Instant.now());
        tokenRepo.save(e);

        // 4) build response
        RevokeResponse resp = new RevokeResponse();
        resp.setSubject(subject);
        resp.setConsentId(consentId);
        resp.setLinked(false); // sau revoke thì không còn liên kết
        resp.setCiamRevoked(ciamRevoked);
        resp.setConsentRevoked(consentRevoked);
        resp.setRevokedAt(e.getRevokedAt());

        return resp;
    }
}
