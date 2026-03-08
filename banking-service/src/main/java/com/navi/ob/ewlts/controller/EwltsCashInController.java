package com.navi.ob.ewlts.controller;

@RestController
@RequestMapping("/api/v1/ewlts")
@RequiredArgsConstructor
public class EwltsCashInController {

    private final EwltsCashInService service;

    /**
     * API “Nạp ví điện tử” - Step (2)-(3)
     */
    @PostMapping("/cash-in")
    public ResponseEntity<?> initCashIn(
            @RequestHeader(value="Request-ID", required=false) String requestId,
            @RequestHeader(value="TPP-ID", required=false) String tppId,
            @RequestHeader(value="Provider-ID", required=false) String providerId,
            @Valid @RequestBody CashInInitRequest req
    ) {
        // NOTE: demo cho phép thiếu headers; production bạn nên bắt buộc Request-ID + TPP-ID
        String rid = (requestId == null || requestId.isBlank()) ? null : requestId.trim();
        String tpp = (tppId == null || tppId.isBlank()) ? "TPP_UNKNOWN" : tppId.trim();

        CashInInitResponse resp = service.initCashIn(rid, tpp, providerId, req);
        return ResponseEntity.ok(resp);
    }

    /**
     * API “Xác nhận OTP” - Step (4)-(5)
     */
    @PostMapping("/cash-in/{paymentId}/otp/verify")
    public ResponseEntity<?> verifyOtp(
            @PathVariable String paymentId,
            @Valid @RequestBody VerifyOtpRequest req
    ) {
        VerifyOtpResponse resp = service.verifyOtpAndDebit(paymentId, req);
        return ResponseEntity.ok(resp);
    }
}
