package com.navi.ob.ais.controller;

import com.navi.ob.ais.dto.AisDtos;
import com.navi.ob.ais.service.AisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AisController {

    private final AisService aisService;

    @GetMapping("/accounts")
    public AisDtos.AccountsResponse listAccounts(
    ) {
        return aisService.listAccounts();
    }

    @PostMapping("/accounts/information")
    public AisDtos.BalanceResponse accountInfo(
            @RequestBody AisDtos.AccountInformationRequest body
    ) {
        return aisService.getBalance(body.accountId());
    }

    @PostMapping("/accounts/transactions")
    public AisDtos.TransactionsResponse transactions(
            @RequestBody AisDtos.AccountTransactionsRequest body
    ) {
        return aisService.getTransactions(
                body.accountId(),
                body.fromDate(),
                body.toDate(),
                body.page(),
                body.size()
        );
    }
}
