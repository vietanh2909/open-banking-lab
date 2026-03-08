package com.navi.controller;

import com.navi.service.AisClientService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ais")
public class AisController {

    private final AisClientService aisClientService;

    public AisController(AisClientService aisClientService) {
        this.aisClientService = aisClientService;
    }

    // tạm thời: subject lấy từ header (sau này thay bằng user login / session)
    private String subjectFromHeader(String xSubject) {
        if (xSubject == null || xSubject.isBlank()) throw new IllegalArgumentException("Missing X-Subject");
        return xSubject;
    }

    @GetMapping("/accounts")
    public Map<?, ?> listAccounts(@RequestHeader("X-Subject") String xSubject) {
        return aisClientService.getAccounts(subjectFromHeader(xSubject));
    }

    @GetMapping("/accounts/{accountId}")
    public Map<?, ?> accountDetail(@RequestHeader("X-Subject") String xSubject,
                                   @PathVariable String accountId) {
        return aisClientService.getAccountDetail(subjectFromHeader(xSubject), accountId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public Map<?, ?> transactions(@RequestHeader("X-Subject") String xSubject,
                                  @PathVariable String accountId,
                                  @RequestParam(required = false) String fromDate,
                                  @RequestParam(required = false) String toDate,
                                  @RequestParam(defaultValue = "0") Integer page,
                                  @RequestParam(defaultValue = "20") Integer size) {
        return aisClientService.getTransactions(subjectFromHeader(xSubject), accountId, fromDate, toDate, page, size);
    }
}
