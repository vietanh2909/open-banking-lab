package com.navi.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "consents")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsentEntity {
    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "consent_type", nullable = false, length = 10)
    private String consentType;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "tpp_id", nullable = false, length = 100)
    private String tppId;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "client_id", nullable = false, length = 50)
    private String clientId;

    @Column(name = "psu_id", length = 128)
    private String psuId;

    @Column(name = "scope", nullable = false, length = 256)
    private String scope;

    @Column(name = "purpose", columnDefinition = "text")
    private String purpose;

    @Column(name = "created_by_actor", nullable = false, length = 20)
    private String createdByActor;

    @Column(name = "request_id", length = 60)
    private String requestId;

    @Column(name = "request_datetime")
    private OffsetDateTime requestDatetime;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @Column(name = "authorised_at")
    private OffsetDateTime authorisedAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "reason_code", length = 50)
    private String reasonCode;

    @Column(name = "reason_detail", columnDefinition = "text")
    private String reasonDetail;

    @Version
    @Column(name = "version", nullable = false)
    private long version;
}
