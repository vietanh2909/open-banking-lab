package com.navi.ob.ewlts.service;

import com.navi.ob.ais.domain.AisAccountEntity;
import com.navi.ob.ais.domain.AisBalanceEntity;
import com.navi.ob.ais.repository.AisAccountRepo;
import com.navi.ob.ais.repository.AisBalanceRepo;
import com.navi.ob.ewlts.domain.EwltsCashInEntity;
import com.navi.ob.ewlts.domain.EwltsCashInOtpEntity;
import com.navi.ob.ewlts.domain.LedgerEntryEntity;
import com.navi.ob.ewlts.dto.CashInInitRequest;
import com.navi.ob.ewlts.dto.CashInInitResponse;
import com.navi.ob.ewlts.dto.VerifyOtpRequest;
import com.navi.ob.ewlts.dto.VerifyOtpResponse;
import com.navi.ob.ewlts.repository.EwltsCashInOtpRepository;
import com.navi.ob.ewlts.repository.EwltsCashInRepository;
import com.navi.ob.ewlts.repository.LedgerEntryRepository;
import com.navi.ob.ewlts.utils.CryptoUtil;
import com.navi.ob.ewlts.utils.IdGen;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EwltsCashInService {

    private static final int OTP_TTL_SECONDS = 300;   // 5 phút
    private static final int OTP_MAX_ATTEMPTS = 5;

    private final AisAccountRepo aisAccountRepo;
    private final AisBalanceRepo aisBalanceRepo;
    private final EwltsCashInRepository cashInRepo;
    private final EwltsCashInOtpRepository otpRepo;
    private final LedgerEntryRepository ledgerRepo;

    /**
     * FLOW (2)-(3): TPP gọi "Nạp ví điện tử" để khởi tạo giao dịch.
     */
    @Transactional
    public CashInInitResponse initCashIn(String requestId, String tppId, String providerId, CashInInitRequest req) {

        // 1) validate account exists
        AisAccountEntity acc = aisAccountRepo.findByAccountId(req.getDebtorAccountId())
                .orElseThrow(() -> new IllegalArgumentException("DEBTOR_ACCOUNT_NOT_FOUND"));

        // 2) validate account status
        if (acc.getStatus() == null || !"active".equalsIgnoreCase(acc.getStatus())) {
            throw new IllegalStateException("DEBTOR_ACCOUNT_NOT_ACTIVE");
        }

        // 3) validate account belongs to PSU (nếu bạn muốn bắt buộc)
        if (req.getPsuId() != null && !req.getPsuId().isBlank()) {
            if (acc.getPsuId() == null || !acc.getPsuId().equals(req.getPsuId())) {
                throw new IllegalStateException("ACCOUNT_NOT_OWNED_BY_PSU");
            }
        }

        // 4) validate currency
        String accCcy = acc.getCurrency() == null ? null : acc.getCurrency().trim();
        if (accCcy == null || !accCcy.equalsIgnoreCase(req.getCurrency())) {
            throw new IllegalStateException("CURRENCY_MISMATCH");
        }

        // 5) validate balance (available_value)
        AisBalanceEntity bal = aisBalanceRepo.findByAccountId(req.getDebtorAccountId())
                .orElseThrow(() -> new IllegalStateException("BALANCE_NOT_FOUND"));

        if (bal.getAvailableValue().compareTo(req.getAmount()) < 0) {
            throw new IllegalStateException("INSUFFICIENT_FUNDS");
        }

        // 6) create payment + otp
        String paymentId = IdGen.paymentId();
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime otpExpiresAt = now.plusSeconds(300);

        cashInRepo.save(EwltsCashInEntity.builder()
                .paymentId(paymentId)
                .requestId(requestId)
                .tppId(tppId)
                .providerId(providerId)
                .psuId(acc.getPsuId())
                .debtorAccountId(req.getDebtorAccountId())
                .ewalletToken(req.getEwalletToken())
                .amount(req.getAmount())
                .currency(req.getCurrency().toUpperCase())
                .status("OTP_PENDING")
                .otpRequired(true)
                .expiredAt(otpExpiresAt)
                .createdAt(now)
                .updatedAt(now)
                .build());

        String otpPlain = IdGen.otp6();
        otpRepo.save(EwltsCashInOtpEntity.builder()
                .paymentId(paymentId)
                .otpHash(CryptoUtil.sha256Hex(paymentId + "|" + otpPlain))
                .otpExpiresAt(otpExpiresAt)
                .attemptCount(0)
                .maxAttempts(5)
                .verified(false)
                .createdAt(now)
                .updatedAt(now)
                .build());

        // demo: log OTP (prod: gửi OTP)
        // log.info("DEBUG OTP paymentId={} otp={}", paymentId, otpPlain);

        return CashInInitResponse.builder()
                .paymentId(paymentId)
                .status("OTP_PENDING")
                .otpRequired(true)
                .otpExpiresInSeconds(300)
                .build();
    }

    /**
     * FLOW (4)-(5): TPP gọi "Xác nhận OTP".
     * Nếu OTP đúng -> trừ tiền + COMPLETED.
     */
    @Transactional
    public VerifyOtpResponse verifyOtpAndDebit(String paymentId, VerifyOtpRequest req) {

        EwltsCashInEntity p = cashInRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("PAYMENT_NOT_FOUND"));

        // Idempotent: đã completed thì trả luôn
        if ("COMPLETED".equals(p.getStatus())) {
            return VerifyOtpResponse.builder()
                    .paymentId(paymentId)
                    .status("COMPLETED")
                    .debitedAmount(p.getAmount())
                    .currency(p.getCurrency())
                    .build();
        }

        if (!"OTP_PENDING".equals(p.getStatus())) {
            throw new IllegalStateException("INVALID_STATUS_FOR_OTP_VERIFY: " + p.getStatus());
        }

        EwltsCashInOtpEntity otp = otpRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalStateException("OTP_NOT_FOUND"));

        OffsetDateTime now = OffsetDateTime.now();

        // expiry / attempts / verify hash
        if (!otp.isVerified()) {
            if (otp.getOtpExpiresAt().isBefore(now)) {
                p.setStatus("EXPIRED");
                p.setReasonCode("OTP_EXPIRED");
                p.setUpdatedAt(now);
                cashInRepo.save(p);
                throw new IllegalStateException("OTP_EXPIRED");
            }

            if (otp.getAttemptCount() >= otp.getMaxAttempts()) {
                p.setStatus("REJECTED");
                p.setReasonCode("OTP_MAX_ATTEMPTS");
                p.setUpdatedAt(now);
                cashInRepo.save(p);
                throw new IllegalStateException("OTP_MAX_ATTEMPTS");
            }

            String candidateHash = CryptoUtil.sha256Hex(paymentId + "|" + req.getOtp());
            if (!candidateHash.equals(otp.getOtpHash())) {
                otp.setAttemptCount(otp.getAttemptCount() + 1);
                otp.setUpdatedAt(now);
                otpRepo.save(otp);
                throw new IllegalArgumentException("OTP_INVALID");
            }

            otp.setVerified(true);
            otp.setVerifiedAt(now);
            otp.setUpdatedAt(now);
            otpRepo.save(otp);

            p.setOtpVerifiedAt(now);
            p.setUpdatedAt(now);
            cashInRepo.save(p);
        }

        // Exactly-once debit using ledger unique index
        if (!ledgerRepo.existsByPaymentIdAndDirection(paymentId, "DEBIT")) {

            // LOCK balance row
            AisBalanceEntity bal = aisBalanceRepo.findByAccountIdForUpdate(p.getDebtorAccountId())
                    .orElseThrow(() -> new IllegalStateException("BALANCE_NOT_FOUND"));

            if (bal.getAvailableValue().compareTo(p.getAmount()) < 0) {
                p.setStatus("FAILED");
                p.setReasonCode("INSUFFICIENT_FUNDS_AT_DEBIT");
                p.setUpdatedAt(now);
                cashInRepo.save(p);
                throw new IllegalStateException("INSUFFICIENT_FUNDS");
            }

            // subtract balance
            bal.setAvailableValue(bal.getAvailableValue().subtract(p.getAmount()));
            bal.setAsOf(now);
            aisBalanceRepo.save(bal);

            // ledger record
            ledgerRepo.save(LedgerEntryEntity.builder()
                    .entryId(UUID.randomUUID())
                    .paymentId(paymentId)
                    .accountId(p.getDebtorAccountId())
                    .direction("DEBIT")
                    .amount(p.getAmount())
                    .currency(p.getCurrency())
                    .createdAt(now)
                    .build());
        }

        // complete
        p.setStatus("COMPLETED");
        p.setCompletedAt(now);
        p.setUpdatedAt(now);
        cashInRepo.save(p);

        return VerifyOtpResponse.builder()
                .paymentId(paymentId)
                .status("COMPLETED")
                .debitedAmount(p.getAmount())
                .currency(p.getCurrency())
                .build();
    }
}
