package com.navi.service;

import com.navi.domain.AccessTokenEntity;
import com.navi.repository.AccessTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TokenStoreService {

    private final AccessTokenRepository repo;

    public TokenStoreService(AccessTokenRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AccessTokenEntity saveToken(String subject, String scope, String accessToken, Instant expiresAt, String refreshToken, String consentId) {
        // nếu muốn 1 subject chỉ có 1 token active -> revoke cái cũ
        repo.findFirstBySubjectAndActiveTrueOrderByCreatedAtDesc(subject)
                .ifPresent(old -> {
                    old.setActive(false);
                    old.setRevokedAt(Instant.now());
                    repo.save(old);
                });

        AccessTokenEntity e = new AccessTokenEntity();
        e.setSubject(subject);
        e.setScope(scope);
        e.setAccessToken(accessToken);
        e.setExpiresAt(expiresAt);
        e.setRefreshToken(refreshToken);
        e.setActive(true);
        e.setConsentId(consentId);
        return repo.save(e);
    }

    public AccessTokenEntity getValidAccessTokenOrThrow(String subject) {
        AccessTokenEntity e = repo.findFirstBySubjectAndActiveTrueOrderByCreatedAtDesc(subject)
                .orElseThrow(() -> new IllegalStateException("No active token for subject=" + subject));

        if (e.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token expired for subject=" + subject);
        }
        return e;
    }

    @Transactional
    public void revoke(String subject) {
        repo.findFirstBySubjectAndActiveTrueOrderByCreatedAtDesc(subject)
                .ifPresent(e -> {
                    e.setActive(false);
                    e.setRevokedAt(Instant.now());
                    repo.save(e);
                });
    }
}