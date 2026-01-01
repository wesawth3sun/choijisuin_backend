package com.finance.service;


import com.finance.service.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {
    void deposit(Long accountId, Long amount);
    void withdraw(Long accountId, Long amount);
    void transfer(Long fromAccountId, String targetAccountNumber, Long amount);
    List<TransactionResponse> getHistory(Long accountId, int page);
}
