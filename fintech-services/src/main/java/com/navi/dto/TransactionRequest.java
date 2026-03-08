package com.navi.dto;

import lombok.Data;

@Data
public class TransactionRequest {
    private String accountId;
    private String fromDate;
    private String toDate;
    private Integer page;
    private Integer size;
}
