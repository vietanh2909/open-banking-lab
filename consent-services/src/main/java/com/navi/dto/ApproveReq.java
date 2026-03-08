package com.navi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ApproveReq {
    public String psuId; // optional
    public List<AccountPerm> accounts;
    public String requestId;
}
