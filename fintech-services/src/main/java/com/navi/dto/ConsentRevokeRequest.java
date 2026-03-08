package com.navi.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ConsentRevokeRequest {
    private String reasonCode;
    private String reasonDetail;
    private Map<String, Object> revokeMeta;
}