package com.finance.infra.repository.impl;

import com.finance.domain.model.Account;
import com.finance.domain.repository.AccountRepository;
import com.finance.infra.entity.AccountEntity;
import com.finance.infra.mapper.AccountMapper;
import com.finance.infra.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;

    @Override
    public Account save(Account account) {
        AccountEntity entity = AccountMapper.toEntity(account);
        accountJpaRepository.save(entity);
        return AccountMapper.toDomain(entity);
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accountJpaRepository.findById(id).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByIdWithLock(Long id) {
        return accountJpaRepository.findByIdWithLock(id).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber)
                .map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumberWithLock(String accountNumber) {
        return accountJpaRepository.findByAccountNumberWithLock(accountNumber)
                .map(AccountMapper::toDomain);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return accountJpaRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public boolean existsById(Long id) {
        return accountJpaRepository.existsById(id);
    }
}
