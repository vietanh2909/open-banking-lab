package com.navi.ob.ais.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ais_accounts")
@Getter
@Setter
public class AisAccountEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "psu_id", nullable = false, length = 64)
    private String psuId;

    @Column(name = "account_id", nullable = false, unique = true, length = 34)
    private String accountId;

    @Column(nullable = false, length = 70)
    private String name;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "bank_code", nullable = false, length = 12)
    private String bankCode;

    @Column(nullable = false, length = 10)
    private String status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
