package com.navi.ob.ewlts.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class VerifyOtpResponse {
    public String paymentId;
    public String status; // COMPLETED
    public BigDecimal debitedAmount;
    public String currency;
}
