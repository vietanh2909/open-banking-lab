package com.navi.ob.ewlts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CashInInitRequest {
    @NotBlank
    public String psuId;

    @NotBlank
    public String debtorAccountId;

    @NotBlank
    public String ewalletToken;

    @NotNull
    public BigDecimal amount;

    @NotBlank
    public String currency;
}
