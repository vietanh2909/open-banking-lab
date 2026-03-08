package com.navi.ob.ais.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public class AisDtos {

    // ===== Requests =====
    public record AccountInformationRequest(String accountId) {}

    public record AccountTransactionsRequest(
            String accountId,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            Integer page,
            Integer size
    ) {}

    // ===== Responses =====
    public record AccountsResponse(List<AccountItem> accounts) {}

    public record AccountItem(
            String accountId,
            String name,
            String type,
            String currency,
            String bankCode,
            String status,
            BigDecimal balance
    ) {}

    public record BalanceResponse(BalanceItem balance) {}

    public record BalanceItem(
            String accountId,
            BigDecimal availableValue,
            String currency,
            OffsetDateTime asOf
    ) {}

    public record TransactionsResponse(
            List<TransactionItem> transactions,
            int page,
            long total
    ) {}

    public record TransactionItem(
            String instructionIdentification,
            OffsetDateTime valueDate,
            BigDecimal amountValue,
            String amountCurrency,
            BigDecimal balanceValue,
            String balanceCurrency,
            String creditDebitIndicator,
            boolean reversalIndicator,
            String debtorName,
            String debtorAccountId,
            String creditorName,
            String creditorAccountId,
            String additionalTransactionInformation
    ) {}
}
