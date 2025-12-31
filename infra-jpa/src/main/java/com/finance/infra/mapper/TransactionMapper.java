package com.finance.infra.mapper;

import com.finance.domain.model.Money;
import com.finance.domain.model.Transaction;
import com.finance.infra.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    /**
     * 도메인(Transaction)을 엔티티(TransactionEntity)로 변환
     */
    public static TransactionEntity toEntity(Transaction domain) {
        return TransactionEntity.builder()
                .accountId(domain.getAccountId())
                .type(domain.getType())
                .amount(domain.getAmount().getAmount())
                .fee(domain.getFee().getAmount())
                .balanceAfterTransaction(domain.getBalanceAfterTransaction().getAmount())
                .targetAccountNumber(domain.getTargetAccountNumber())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    /**
     * 엔티티(TransactionEntity)를 도메인(Transaction)으로 변환
     */
    public static Transaction toDomain(TransactionEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .accountId(entity.getAccountId())
                .type(entity.getType())
                .amount(new Money(entity.getAmount()))
                .fee(new Money(entity.getFee()))
                .balanceAfterTransaction(new Money(entity.getBalanceAfterTransaction()))
                .targetAccountNumber(entity.getTargetAccountNumber())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}