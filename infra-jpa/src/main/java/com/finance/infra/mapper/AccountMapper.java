package com.finance.infra.mapper;

import com.finance.domain.model.Account;
import com.finance.domain.model.Money;
import com.finance.infra.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    /**
     * 도메인 객체를 엔티티로 변환
     * @param account 계좌 도메인 객체
     * @return AccountEntity
     */
    public static AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance().getAmount().longValue())
                .deleted(account.isDeleted())
                .createdAt(account.getCreatedAt())
                .currency(account.getCurrency())
                .build();
    }

    /**
     * 엔티티를 도메인 객체로 변환
     * @param entity 계좌 엔티티
     * @return 계좌 도메인
     */
    public static Account toDomain(AccountEntity entity) {
        return Account.builder()
                .id(entity.getId())
                .accountNumber(entity.getAccountNumber())
                .balance(Money.of(entity.getBalance()))
                .deleted(entity.isDeleted())
                .createdAt(entity.getCreatedAt())
                .currency(entity.getCurrency())
                .build();
    }
}
