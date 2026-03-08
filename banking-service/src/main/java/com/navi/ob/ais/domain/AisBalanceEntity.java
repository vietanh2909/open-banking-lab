package com.navi.ob.ais.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ais_balances",
        uniqueConstraints = @UniqueConstraint(name = "uk_ais_balance_account", columnNames = "account_id")
)
@Getter
@Setter
public class AisBalanceEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "account_id", nullable = false, length = 34)
    private String accountId;

    @Column(name = "available_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal availableValue;

    @Column(nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String currency;

    @Column(name = "as_of", nullable = false)
    private OffsetDateTime asOf;
}
