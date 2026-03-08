package com.navi.ob.ewlts.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@Table(name="ewlts_cash_in")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EwltsCashInEntity {

    @Id
    @Column(name="payment_id", length=35)
    private String paymentId;

    @Column(name="request_id", length=60)
    private String requestId;

    @Column(name="tpp_id", nullable=false, length=15)
    private String tppId;

    @Column(name="provider_id", length=8)
    private String providerId;

    @Column(name="client_id", length=50)
    private String clientId;

    @Column(name="psu_id", nullable=false, length=128)
    private String psuId;

    @Column(name="debtor_account_id", nullable=false, length=34)
    private String debtorAccountId;

    @Column(name="ewallet_token", nullable=false, length=30)
    private String ewalletToken;

    @Column(name="amount", nullable=false, precision=18, scale=2)
    private BigDecimal amount;

    @Column(name="currency", nullable=false, length=3)
    private String currency;

    @Column(name="status", nullable=false, length=30)
    private String status; // CREATED|OTP_PENDING|REJECTED|COMPLETED|FAILED|EXPIRED

    @Column(name="otp_required", nullable=false)
    private boolean otpRequired;

    @Column(name="otp_verified_at")
    private OffsetDateTime otpVerifiedAt;

    @Column(name="completed_at")
    private OffsetDateTime completedAt;

    @Column(name="expired_at")
    private OffsetDateTime expiredAt;

    @Column(name="reason_code", length=50)
    private String reasonCode;

    @Column(name="reason_detail", columnDefinition="text")
    private String reasonDetail;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name="version", nullable=false)
    private long version;
}