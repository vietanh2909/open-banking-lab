package com.navi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class DenyReq {
    public String reasonCode;
    public String reasonDetail;
    public String requestId;
}
