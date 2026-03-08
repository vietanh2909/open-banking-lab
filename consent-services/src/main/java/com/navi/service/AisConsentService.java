package com.navi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navi.domain.*;
import com.navi.dto.InitReq;
import com.navi.exception.ConflictException;
import com.navi.exception.NotFoundException;
import com.navi.repository.ConsentAccountRepository;
import com.navi.repository.ConsentRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AisConsentService {

    private final ConsentRepository consentRepo;
    private final ConsentAccountRepository accountRepo;
    private final EventService eventService;
    private final ObjectMapper om = new ObjectMapper();

    // ---------- DTOs used internally ----------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor @Builder
    public static class InitRequest {
        public String tppId;
        public String providerId;
        public String clientId;
        public String scopeText;      // e.g. "AIS" or "openid profile AIS"
        public String purpose;
        public String requestId;
        public OffsetDateTime requestDatetime;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApproveRequest {
        public String psuId; // optional if already set
        public List<AccountPermission> accounts;
        public String requestId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AccountPermission {
        public String accountId;
        public Map<String, Object> permissions; // e.g. {"balances":true,"transactions":true}
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DenyRequest {
        public String reasonCode;
        public String reasonDetail;
        public String requestId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class RevokeRequest {
        public String reasonCode;
        public String reasonDetail;
        public Map<String,Object> revokeMeta;
        public String requestId;
    }

    // ---------- Methods ----------

    @Transactional
    public UUID init(InitReq req) {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        ConsentEntity e = ConsentEntity.builder()
                .id(id)
                .consentType(ConsentType.AIS.name())
                .status(ConsentStatus.CREATED.name())
                .tppId(require(req.tppId, "tppId"))
                .providerId(req.providerId)
                .clientId(require(req.clientId, "clientId"))
                .psuId(null)
                .scope(require(req.scopeText, "scopeText"))
                .purpose(req.purpose)
                .createdByActor(ActorType.TPP.name())
                .requestId(req.requestId)
                .requestDatetime(req.requestDatetime)
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .build();

        consentRepo.save(e);
        eventService.append(id, EventType.CREATED, ActorType.TPP, req.requestId, Map.of(
                "consentType","AIS",
                "clientId", req.clientId,
                "scopeText", req.scopeText
        ));

        return id;
    }



    @Transactional
    public void approve(UUID consentId, ApproveRequest req) {
        ConsentEntity c = get(consentId);
        // Only allow approve from CREATED
        ensureState(c, Set.of(ConsentStatus.CREATED), "approve");

        // psuId may be provided at approve-time (since you're skipping Keycloak now)
        if (req.psuId != null && !req.psuId.isBlank()) {
            c.setPsuId(req.psuId);
        }
        if (c.getPsuId() == null || c.getPsuId().isBlank()) {
            throw new ConflictException("psuId is required before approve");
        }

        // Save accounts (replace all)
        accountRepo.deleteByConsentId(consentId);
        if (req.accounts != null) {
            for (AccountPermission ap : req.accounts) {
                if (ap.accountId == null || ap.accountId.isBlank()) continue;
                String permStr = toJsonSafe(ap.permissions == null ? Map.of() : ap.permissions);
                JsonNode permJson = permStr == null ? null : om.valueToTree(permStr);
                accountRepo.save(ConsentAccountEntity.builder()
                        .consentId(consentId)
                        .accountId(ap.accountId)
                        .permissionsJson(permJson)
                        .createdAt(OffsetDateTime.now())
                        .build());
            }
        }
        // Update consent status
        c.setStatus(ConsentStatus.APPROVED.name());
        c.setAuthorisedAt(OffsetDateTime.now());
        c.setUpdatedAt(OffsetDateTime.now());
        consentRepo.save(c);

        eventService.append(consentId, EventType.APPROVED, ActorType.PSU, req.requestId, Map.of(
                "accountsCount", req.accounts == null ? 0 : req.accounts.size()
        ));
    }

    @Transactional
    public void deny(UUID consentId, DenyRequest req) {
        ConsentEntity c = get(consentId);
        ensureState(c, Set.of(ConsentStatus.CREATED), "deny");

        c.setStatus(ConsentStatus.REJECTED.name());
        c.setReasonCode(req.reasonCode);
        c.setReasonDetail(req.reasonDetail);
        c.setUpdatedAt(OffsetDateTime.now());
        consentRepo.save(c);

        eventService.append(consentId, EventType.REJECTED, ActorType.PSU, req.requestId, Map.of(
                "reasonCode", req.reasonCode,
                "reasonDetail", req.reasonDetail
        ));
    }


    @Transactional
    public void revoke(UUID consentId, RevokeRequest req) {
        ConsentEntity c = get(consentId);
        // idempotent revoke: if already revoked, just write event once if you want; here we keep idempotent (no throw)
        if (ConsentStatus.REVOKED.name().equals(c.getStatus())) {
            eventService.append(consentId, EventType.REVOKED, ActorType.TPP, req.requestId, Map.of(
                    "alreadyRevoked", true,
                    "revokeMeta", req.revokeMeta
            ));
            return;
        }

        // allow revoke from ACTIVE (and even AUTHORISED depending on your policy)
        ensureState(c, Set.of(ConsentStatus.APPROVED), "revoke");

        c.setStatus(ConsentStatus.REVOKED.name());
        c.setRevokedAt(OffsetDateTime.now());
        c.setReasonCode(req.reasonCode);
        c.setReasonDetail(req.reasonDetail);
        c.setUpdatedAt(OffsetDateTime.now());
        consentRepo.save(c);

        eventService.append(consentId, EventType.REVOKED, ActorType.TPP, req.requestId, Map.of(
                "reasonCode", req.reasonCode,
                "reasonDetail", req.reasonDetail,
                "revokeMeta", req.revokeMeta
        ));
    }

    public ConsentEntity get(UUID consentId) {
        return consentRepo.findById(consentId).orElseThrow(() ->
                new NotFoundException("Consent not found: " + consentId));
    }

    public Optional<ConsentEntity> findActive(String psuId, String tppId, String clientId) {
        return consentRepo.findActiveAisConsent(psuId, tppId, clientId);
    }

    // ---------- Helpers ----------
    private static String require(String s, String field) {
        if (s == null || s.isBlank()) throw new ConflictException(field + " is required");
        return s;
    }

    private static void ensureState(ConsentEntity c, Set<ConsentStatus> allowed, String action) {
        ConsentStatus current = ConsentStatus.valueOf(c.getStatus());
        if (!allowed.contains(current)) {
            throw new ConflictException("Invalid state for " + action + ": " + current);
        }
    }

    private String toJsonSafe(Object o) {
        try { return om.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }
}
