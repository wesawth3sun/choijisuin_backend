package com.finance.domain.repository;


import com.finance.domain.model.Money;
import com.finance.domain.model.Transaction;
import com.finance.domain.model.e.TransactionType;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository {
    void save(Transaction domain);
    List<Transaction> getHistory(Long accountId, Pageable page);
    Money findDailySumByType(Long accountId, LocalDateTime time, TransactionType type);
}
