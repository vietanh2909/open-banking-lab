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
        name = "ais_transactions",
        indexes = {
                @Index(name = "idx_ais_tx_acc_date", columnList = "account_id, value_date")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_ais_tx_instruction", columnNames = "instruction_identification")
)
@Getter
@Setter
public class AisTransactionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "account_id", nullable = false, length = 34)
    private String accountId;

    @Column(name = "instruction_identification", nullable = false, length = 70)
    private String instructionIdentification;

    @Column(name = "value_date", nullable = false)
    private OffsetDateTime valueDate;

    @Column(name = "amount_value", nullable = false, precision = 20, scale = 2)
    private BigDecimal amountValue;

    @Column(name = "amount_currency", nullable = false, length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String amountCurrency;

    @Column(name = "balance_value", precision = 20, scale = 2)
    private BigDecimal balanceValue;

    @Column(name = "balance_currency", length = 3)
    @JdbcTypeCode(SqlTypes.CHAR)
    private String balanceCurrency;

    // CRDT / DBIT
    @Column(name = "credit_debit_indicator", nullable = false, length = 4)
    private String creditDebitIndicator;

    @Column(name = "reversal_indicator", nullable = false)
    private boolean reversalIndicator;

    @Column(name = "debtor_name", length = 70)
    private String debtorName;

    @Column(name = "debtor_account_id", length = 34)
    private String debtorAccountId;

    @Column(name = "creditor_name", length = 70)
    private String creditorName;

    @Column(name = "creditor_account_id", length = 34)
    private String creditorAccountId;

    @Column(name = "additional_transaction_information", length = 255)
    private String additionalTransactionInformation;
}