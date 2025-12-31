package com.finance.domain.repository;


import com.finance.domain.model.Account;

import java.util.Optional;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(Long id);
    Optional<Account> findByIdWithLock(Long id); // 비관적 락 적용
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumberWithLock(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsById(Long id);
}
