package com.finance.infra.repository.impl;

import com.finance.domain.model.Money;
import com.finance.domain.model.Transaction;
import com.finance.domain.model.e.TransactionType;
import com.finance.domain.repository.TransactionRepository;
import com.finance.infra.mapper.TransactionMapper;
import com.finance.infra.repository.TransactionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    @Override
    public void save(Transaction domain) {
        jpaRepository.save(TransactionMapper.toEntity(domain));
    }

    @Override
    public List<Transaction> getHistory(Long accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        // 계좌 기반의 거래 내역을 최신순 정렬해서 가져옴
        return jpaRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .stream()
                .map(TransactionMapper::toDomain)
                .toList();
    }

    @Override
    public Money findDailySumByType(Long accountId, LocalDateTime time, TransactionType type) {
        BigDecimal dailyWithdrawSum = jpaRepository.findDailySumByType(accountId, time, type);

        // BigDecimal -> long 변환
        long longValue = dailyWithdrawSum == null ? 0L : dailyWithdrawSum.longValue();

        return Money.of(longValue);
    }
}
