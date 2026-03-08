package com.navi.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.navi.util.JsonNodeConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

@Entity
@Table(name = "consent_accounts",
        uniqueConstraints = @UniqueConstraint(name="uq_consent_accounts", columnNames = {"consent_id","account_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConsentAccountEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_id", nullable = false, columnDefinition = "uuid")
    private java.util.UUID consentId;

    @Column(name = "account_id", nullable = false, length = 34)
    private String accountId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "permissions", nullable = false, columnDefinition = "jsonb")
    private JsonNode permissionsJson;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
