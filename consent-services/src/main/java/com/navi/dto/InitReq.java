package com.navi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class InitReq {
    @NotBlank
    public String tppId;
    public String providerId;
    @NotBlank
    public String clientId;
    @NotBlank
    public String scopeText;
    public String purpose;
    public String requestId;

    public OffsetDateTime requestDatetime;
}
