package com.navi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.navi.util.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "idempotency_keys")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class IdempotencyKeyEntity {

    @Id
    @Column(name = "request_id", length = 60)
    private String requestId;

    @Column(name = "api_scope", nullable = false, length = 50)
    private String apiScope;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_code", nullable = false)
    private int responseCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private JsonNode responseBodyJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
}
