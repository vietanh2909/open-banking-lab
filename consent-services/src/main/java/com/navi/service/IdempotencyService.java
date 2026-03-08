package com.navi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navi.domain.IdempotencyKeyEntity;
import com.navi.repository.IdempotencyKeyRepository;
import com.navi.util.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {
    private final IdempotencyKeyRepository repo;

    private final ObjectMapper om = new ObjectMapper();

    public Optional<IdempotencyKeyEntity> find(String requestId) {
        if (requestId == null || requestId.isBlank()) return Optional.empty();
        return repo.findById(requestId);
    }

    public String requestHash(String apiScope, String bodyCanonical) {
        return Hashing.sha256Hex(apiScope + "|" + bodyCanonical);
    }

    public void save(String requestId, String apiScope, String requestHash,
                     int responseCode, JsonNode responseBodyJson,
                     OffsetDateTime ttlUntil) {
        if (requestId == null || requestId.isBlank()) return;

        repo.save(IdempotencyKeyEntity.builder()
                .requestId(requestId)
                .apiScope(apiScope)
                .requestHash(requestHash)
                .responseCode(responseCode)
                .responseBodyJson(responseBodyJson)
                .createdAt(OffsetDateTime.now())
                .expiresAt(ttlUntil)
                .build());
    }
}
