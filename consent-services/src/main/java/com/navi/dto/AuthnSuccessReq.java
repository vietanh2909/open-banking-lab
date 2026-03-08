package com.navi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class AuthnSuccessReq {
    @NotBlank
    public String psuId;
    public String requestId;
}
