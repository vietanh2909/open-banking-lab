package com.navi.dto;

import lombok.*;
import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class ActivatedReq {
    public OffsetDateTime validUntil;
    public Map<String,Object> tokenMeta;
    public String requestId;
}
