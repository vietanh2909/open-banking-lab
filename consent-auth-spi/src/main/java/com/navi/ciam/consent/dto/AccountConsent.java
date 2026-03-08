package com.navi.ciam.consent.dto;

public class AccountConsent {
    private String accountId;
    private Permission permissions;

    public AccountConsent() {}

    public String getAccountId() {
        return accountId;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setPermissions(Permission permissions) {
        this.permissions = permissions;
    }
}
