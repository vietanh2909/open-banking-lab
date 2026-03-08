package com.navi.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navi.domain.IdempotencyKeyEntity;
import com.navi.dto.*;
import com.navi.service.AisConsentService;
import com.navi.service.IdempotencyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/internal/v1/ais/consents")
public class AisConsentController {

    private final AisConsentService service;
    private final IdempotencyService idempotency;

    public AisConsentController(AisConsentService service, IdempotencyService idempotency) {
        this.service = service;
        this.idempotency = idempotency;
    }
    private final ObjectMapper om = new ObjectMapper();
    @PostMapping("/init")
    public ResponseEntity<?> init(@RequestHeader(value="Request-ID", required=false) String hdrRequestId,
                                  @Valid @RequestBody InitReq req) throws Exception {
        String requestId = (hdrRequestId != null && !hdrRequestId.isBlank()) ? hdrRequestId : req.requestId;

        // Idempotency (30m)
        String canonical = om.writeValueAsString(req);
        String hash = idempotency.requestHash("AIS_INIT", canonical); // hash request

        Optional<IdempotencyKeyEntity> existing = idempotency.find(requestId);
        if (existing.isPresent()) {
            // (optional but recommended) chống reuse Request-ID với payload khác
            if (!hash.equals(existing.get().getRequestHash())) {
                return ResponseEntity.status(409).body(Map.of(
                        "error", "IDEMPOTENCY_CONFLICT",
                        "message", "Same Request-ID with different request payload"
                ));
            }

            // return stored response as-is
            return ResponseEntity.status(existing.get().getResponseCode())
                    .body(existing.get().getResponseBodyJson());
        }

        UUID consentId = service.init(InitReq.builder()
                .tppId(req.tppId)
                .providerId(req.providerId)
                .clientId(req.clientId)
                .scopeText(req.scopeText)
                .purpose(req.purpose)
                .requestId(requestId)
                .requestDatetime(req.requestDatetime)
                .build());

        Map<String,Object> resp = Map.of(
                "consentId", consentId.toString(),
                "status", "CREATED"
        );

        String respJson = om.writeValueAsString(resp);

        JsonNode responseJson = om.valueToTree(resp);;

        // tránh retry ngắn hạn và double process
        idempotency.save(requestId, "AIS_INIT", hash, 200, responseJson, OffsetDateTime.now().plus(30, ChronoUnit.MINUTES));
        return ResponseEntity.ok(resp);
    }


    @PostMapping("/{consentId}/approve")
    public ResponseEntity<?> approve(@PathVariable UUID consentId,
                                     @RequestHeader(value="Request-ID", required=false) String hdrRequestId,
                                     @RequestBody ApproveReq req) {
        String requestId = (hdrRequestId != null && !hdrRequestId.isBlank()) ? hdrRequestId : req.requestId;

        List<AisConsentService.AccountPermission> accounts = new ArrayList<>();
        if (req.accounts != null) {
            for (AccountPerm ap : req.accounts) {
                accounts.add(AisConsentService.AccountPermission.builder()
                        .accountId(ap.accountId)
                        .permissions(ap.permissions == null ? Map.of() : ap.permissions)
                        .build());
            }
        }

        service.approve(consentId, AisConsentService.ApproveRequest.builder()
                .psuId(req.psuId)
                .accounts(accounts)
                .requestId(requestId)
                .build());

        return ResponseEntity.ok(Map.of("consentId", consentId.toString(), "status", "APPROVED"));
    }

    @PostMapping("/{consentId}/deny")
    public ResponseEntity<?> deny(@PathVariable UUID consentId,
                                  @RequestHeader(value="Request-ID", required=false) String hdrRequestId,
                                  @RequestBody DenyReq req) {
        String requestId = (hdrRequestId != null && !hdrRequestId.isBlank()) ? hdrRequestId : req.requestId;

        service.deny(consentId, AisConsentService.DenyRequest.builder()
                .reasonCode(req.reasonCode)
                .reasonDetail(req.reasonDetail)
                .requestId(requestId)
                .build());

        return ResponseEntity.ok(Map.of("consentId", consentId.toString(), "status", "REJECTED"));
    }


    @PostMapping("/{consentId}/revoked")
    public ResponseEntity<?> revoked(@PathVariable UUID consentId,
                                     @RequestHeader(value="Request-ID", required=false) String hdrRequestId,
                                     @RequestBody RevokeReq req) {
        String requestId = (hdrRequestId != null && !hdrRequestId.isBlank()) ? hdrRequestId : req.requestId;

        service.revoke(consentId, AisConsentService.RevokeRequest.builder()
                .reasonCode(req.reasonCode)
                .reasonDetail(req.reasonDetail)
                .revokeMeta(req.revokeMeta == null ? Map.of() : req.revokeMeta)
                .requestId(requestId)
                .build());

        return ResponseEntity.ok(Map.of("consentId", consentId.toString(), "status", "REVOKED"));
    }
}
