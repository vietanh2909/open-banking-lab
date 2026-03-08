package com.navi.ob.ewlts.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CashInInitResponse {
    public String paymentId;
    public String status; // OTP_PENDING
    public boolean otpRequired;
    public long otpExpiresInSeconds;
}