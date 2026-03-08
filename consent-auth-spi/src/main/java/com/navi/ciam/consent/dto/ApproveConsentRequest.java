package com.navi.ciam.consent.dto;

import java.util.List;

public class ApproveConsentRequest {
    private String psuId;
    private List<AccountConsent> accounts;

    public ApproveConsentRequest() {}

    public List<AccountConsent> getAccounts() {
        return accounts;
    }

    public String getPsuId() {
        return psuId;
    }

    public void setAccounts(List<AccountConsent> accounts) {
        this.accounts = accounts;
    }

    public void setPsuId(String psuId) {
        this.psuId = psuId;
    }
}
