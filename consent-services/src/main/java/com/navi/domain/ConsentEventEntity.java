package com.navi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.navi.util.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consent_events")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConsentEventEntity {

    @Id
    @Column(name = "event_id", columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "consent_id", nullable = false, columnDefinition = "uuid")
    private UUID consentId;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @Column(name = "request_id", length = 60)
    private String requestId;

    @Column(name = "actor", nullable = false, length = 20)
    private String actor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb")
    private JsonNode payloadJson;

    @Column(name = "published", nullable = false)
    private boolean published;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;
}
