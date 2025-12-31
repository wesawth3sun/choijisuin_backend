package com.finance.infra.mapper;

import com.finance.domain.model.Money;
import com.finance.domain.model.ScheduledTransfer;
import com.finance.infra.entity.ScheduledTransferEntity;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTransferMapper {

    /**
     * 도메인 객체를 엔티티로 변환
     * @param domain 예약 이체 도메인 객체
     * @return ScheduledTransferEntity
     */
    public static com.finance.infra.entity.ScheduledTransferEntity toEntity(ScheduledTransfer domain) {
        return ScheduledTransferEntity.builder()
                .id(domain.getId())
                .fromAccountId(domain.getFromAccountId())
                .toAccountNumber(domain.getToAccountNumber())
                .amount(domain.getAmount().getAmount())
                .type(domain.getType())
                .executionTime(domain.getExecutionTime())
                .recurringDay(domain.getRecurringDay())
                .status(domain.getStatus())
                .build();

    }

    /**
     * 엔티티를 도메인 객체로 변환
     * @param entity 예약 이체 엔티티
     * @return ScheduledTransfer
     */
    public static ScheduledTransfer toDomain(ScheduledTransferEntity entity) {
        return ScheduledTransfer.builder()
                .id(entity.getId())
                .fromAccountId(entity.getFromAccountId())
                .toAccountNumber(entity.getToAccountNumber())
                .amount(new Money(entity.getAmount()))
                .type(entity.getType())
                .executionTime(entity.getExecutionTime())
                .recurringDay(entity.getRecurringDay())
                .status(entity.getStatus())
                .build();
    }
}
