package com.navi.ob.ewlts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="ewlts_cash_in_otp")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EwltsCashInOtpEntity {
    @Id
    @Column(name="payment_id", length=35)
    private String paymentId;

    @Column(name="otp_hash", nullable=false, length=128)
    private String otpHash;

    @Column(name="otp_expires_at", nullable=false)
    private OffsetDateTime otpExpiresAt;

    @Column(name="attempt_count", nullable=false)
    private int attemptCount;

    @Column(name="max_attempts", nullable=false)
    private int maxAttempts;

    @Column(name="verified", nullable=false)
    private boolean verified;

    @Column(name="verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at", nullable=false)
    private OffsetDateTime updatedAt;
}