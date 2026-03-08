package com.navi.ob.ewlts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="ledger_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryEntity {
    @Id
    @Column(name="entry_id", columnDefinition="uuid")
    private UUID entryId;

    @Column(name="payment_id", nullable=false, length=35)
    private String paymentId;

    @Column(name="account_id", nullable=false, length=34)
    private String accountId;

    @Column(name="direction", nullable=false, length=10)
    private String direction; // DEBIT|CREDIT

    @Column(name="amount", nullable=false, precision=18, scale=2)
    private BigDecimal amount;

    @Column(name="currency", nullable=false, length=3)
    private String currency;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;
}