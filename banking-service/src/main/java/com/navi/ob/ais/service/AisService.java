package com.navi.ob.ais.service;

import com.navi.ob.ais.domain.AisAccountEntity;
import com.navi.ob.ais.domain.AisBalanceEntity;
import com.navi.ob.ais.domain.AisTransactionEntity;
import com.navi.ob.ais.dto.AisDtos;
import com.navi.ob.ais.repository.AisAccountRepo;
import com.navi.ob.ais.repository.AisBalanceRepo;
import com.navi.ob.ais.repository.AisTransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AisService {

    //private final ConsentService consentService;
    private final AisAccountRepo accountRepo;
    private final AisBalanceRepo balanceRepo;
    private final AisTransactionRepo txRepo;

    public AisDtos.AccountsResponse listAccounts() {
        //ConsentCtx c = consentService.validateAccessToken(authorization, tppId);
        //if (!c.valid()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CONSENT_INVALID");

        //List<AisAccountEntity> accounts = accountRepo.findByAccountIdIn(c.allowedAccounts());
        List<AisAccountEntity> accounts = accountRepo.findAll();

        List<String> accountIds = accounts.stream()
                .map(AisAccountEntity::getAccountId)
                .toList();

        // lấy balance theo list accountIds
        List<AisBalanceEntity> balances = balanceRepo.findByAccountIdIn(accountIds);

        Map<String, BigDecimal> balanceMap =
                balances.stream()
                        .collect(Collectors.toMap(
                                AisBalanceEntity::getAccountId,
                                AisBalanceEntity::getAvailableValue
                        ));

        List<AisDtos.AccountItem> items = accounts.stream()
                .map(account -> new AisDtos.AccountItem(
                        account.getAccountId(),
                        account.getName(),
                        account.getType(),
                        account.getCurrency(),
                        account.getBankCode(),
                        account.getStatus(),
                        balanceMap.get(account.getAccountId()) // null nếu không có balance
                ))
                .toList();

        return new AisDtos.AccountsResponse(items);
    }

    public AisDtos.BalanceResponse getBalance(String accountId) {


        AisBalanceEntity b = balanceRepo.findByAccountId(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "BALANCE_NOT_FOUND"));

        return new AisDtos.BalanceResponse(new AisDtos.BalanceItem(
                b.getAccountId(), b.getAvailableValue(), b.getCurrency(), b.getAsOf()
        ));
    }

    public AisDtos.TransactionsResponse getTransactions(
            String accountId,
            OffsetDateTime from,
            OffsetDateTime to,
            Integer page,
            Integer size
    ) {
        int p = page == null ? 0 : Math.max(page, 0);
        int s = size == null ? 20 : Math.min(Math.max(size, 1), 200);

        Page<AisTransactionEntity> result = txRepo.findByAccountIdAndValueDateBetweenOrderByValueDateDesc(
                accountId, from, to, PageRequest.of(p, s)
        );

        List<AisDtos.TransactionItem> txs = result.getContent().stream()
                .map(t -> new AisDtos.TransactionItem(
                        t.getInstructionIdentification(),
                        t.getValueDate(),
                        t.getAmountValue(),
                        t.getAmountCurrency(),
                        t.getBalanceValue(),
                        t.getBalanceCurrency(),
                        t.getCreditDebitIndicator(),
                        t.isReversalIndicator(),
                        t.getDebtorName(),
                        t.getDebtorAccountId(),
                        t.getCreditorName(),
                        t.getCreditorAccountId(),
                        t.getAdditionalTransactionInformation()
                ))
                .toList();

        return new AisDtos.TransactionsResponse(txs, result.getNumber(), result.getTotalElements());
    }
}
