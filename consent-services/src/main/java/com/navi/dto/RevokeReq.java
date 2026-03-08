package com.navi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class RevokeReq {
    public String reasonCode;
    public String reasonDetail;
    public Map<String,Object> revokeMeta;
    public String requestId;
}
