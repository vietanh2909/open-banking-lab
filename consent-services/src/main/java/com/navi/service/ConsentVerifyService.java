package com.navi.service;

import com.navi.domain.ConsentEntity;
import com.navi.domain.ConsentType;
import com.navi.dto.VerifyConsentResponse;
import com.navi.exception.ConsentVerifyException;
import com.navi.repository.ConsentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class ConsentVerifyService {
    private final ConsentRepository consentRepository;
    private final String providerId;

    public ConsentVerifyService(
            ConsentRepository consentRepository,
            @Value("${consent.provider-id}") String providerId
    ) {
        this.consentRepository = consentRepository;
        this.providerId = providerId;
    }

    public VerifyConsentResponse verify(Jwt jwt, String consentId) {

        // 0) validate request
        UUID consentUuid = parseConsentId(consentId);

        // 1) verify token not expired (Resource Server thường đã check, nhưng double-check cho chắc)
        Instant exp = jwt.getExpiresAt();
        if (exp == null) {
            throw new ConsentVerifyException("INVALID_TOKEN", "Missing exp claim.");
        }
        if (exp.isBefore(Instant.now())) {
            throw new ConsentVerifyException("TOKEN_EXPIRED", "Access token has expired.");
        }

        // 2) extract required claims
        String clientId = trimToNull(jwt.getClaimAsString("azp")); // client_id
        String psuId = trimToNull(jwt.getClaimAsString("preferred_username"));
        String scopeStr = trimToNull(jwt.getClaimAsString("scope"));

        if (clientId == null) {
            throw new ConsentVerifyException("INVALID_TOKEN", "Missing azp (client_id) claim.");
        }
        if (psuId == null) {
            throw new ConsentVerifyException("INVALID_TOKEN", "Missing preferred_username (psu_id) claim.");
        }
        if (scopeStr == null) {
            throw new ConsentVerifyException("INVALID_SCOPE", "Missing scope claim.");
        }

        // 3) parse scopes
        Set<String> tokenScopes = parseScopes(scopeStr);
        if (!tokenScopes.contains("ais")) {
            throw new ConsentVerifyException("INVALID_SCOPE", "Token scope must contain 'ais'.");
        }

        // 4) load consent by consent_id
        ConsentEntity consent = consentRepository.findById(consentUuid)
                .orElseThrow(() -> new ConsentVerifyException(
                        "CONSENT_NOT_FOUND",
                        "Consent not found: " + consentUuid
                ));

        // 5) validate consent_type
        if (consent.getConsentType() != ConsentType.AIS) {
            throw new ConsentVerifyException("INVALID_CONSENT_TYPE", "Consent type must be AIS.");
        }

        // 6) validate status
        if (consent.getStatus() != ConsentStatus.APPROVED) {
            throw new ConsentVerifyException("INVALID_STATUS", "Consent must be APPROVED.");
        }

        // 7) validate revoked/cancelled (optional nhưng nên có)
        if (consent.getRevokedAt() != null) {
            throw new ConsentVerifyException("CONSENT_REVOKED", "Consent has been revoked.");
        }
        if (consent.getCancelledAt() != null) {
            throw new ConsentVerifyException("CONSENT_CANCELLED", "Consent has been cancelled.");
        }

        // 8) validate client_id / provider_id / psu_id
        if (!equalsNormalized(consent.getClientId(), clientId)) {
            throw new ConsentVerifyException("CLIENT_MISMATCH", "Token azp does not match consent.client_id.");
        }
        if (!equalsNormalized(consent.getProviderId(), providerId)) {
            // Nếu bạn có claim provider_id thì thay providerId bằng claim để so sánh
            throw new ConsentVerifyException("PROVIDER_MISMATCH", "Provider mismatch.");
        }
        if (!equalsNormalized(consent.getPsuId(), psuId)) {
            throw new ConsentVerifyException("PSU_MISMATCH", "Token preferred_username does not match consent.psu_id.");
        }

        // 9) validate scope: consentScopes ⊆ tokenScopes
        String consentScopeStr = trimToNull(consent.getScope());
        if (consentScopeStr == null) {
            throw new ConsentVerifyException("INVALID_SCOPE", "Consent scope is empty.");
        }

        Set<String> consentScopes = parseScopes(consentScopeStr);
        if (!tokenScopes.containsAll(consentScopes)) {
            throw new ConsentVerifyException("INVALID_SCOPE", "Token scopes do not cover consent scopes.");
        }

        // 10) OK
        return new VerifyConsentResponse(
                true,
                consent.getId().toString(),
                consent.getConsentType().name(),
                consent.getStatus().name(),
                consent.getClientId(),
                consent.getProviderId(),
                consent.getPsuId(),
                consent.getScope()
        );
    }

    // ========================= helpers =========================

    private UUID parseConsentId(String consentId) {
        String v = trimToNull(consentId);
        if (v == null) {
            throw new ConsentVerifyException("INVALID_REQUEST", "Missing X-Consent-Id header.");
        }
        try {
            return UUID.fromString(v);
        } catch (IllegalArgumentException e) {
            throw new ConsentVerifyException("INVALID_REQUEST", "X-Consent-Id is not a valid UUID: " + v);
        }
    }

    private Set<String> parseScopes(String scopes) {
        return Arrays.stream(scopes.split("\\s+"))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean equalsNormalized(String a, String b) {
        return Objects.equals(trimToNull(a), trimToNull(b));
    }
}
