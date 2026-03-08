package com.navi.ciam.consent.dto;

public class Permission {
    private boolean balances;
    private boolean transactions;

    public Permission() {}

    public boolean isBalances() {
        return balances;
    }

    public boolean isTransactions() {
        return transactions;
    }

    public void setBalances(boolean balances) {
        this.balances = balances;
    }

    public void setTransactions(boolean transactions) {
        this.transactions = transactions;
    }
}
