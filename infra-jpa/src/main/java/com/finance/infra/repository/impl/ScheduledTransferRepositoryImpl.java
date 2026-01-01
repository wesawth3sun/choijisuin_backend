package com.finance.infra.repository.impl;

import com.finance.domain.model.ScheduledTransfer;
import com.finance.domain.model.e.TransferStatus;
import com.finance.domain.model.e.TransferType;
import com.finance.domain.repository.ScheduledTransferRepository;
import com.finance.infra.mapper.ScheduledTransferMapper;
import com.finance.infra.repository.ScheduledTransferJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ScheduledTransferRepositoryImpl implements ScheduledTransferRepository {

    private final ScheduledTransferJpaRepository jpaRepository;

    @Override
    public void save(ScheduledTransfer domain) {
        jpaRepository.save(ScheduledTransferMapper.toEntity(domain));
    }

    @Override
    public List<ScheduledTransfer> findAllByExecutionTimeBeforeAndStatusAndType(LocalDateTime now,
                                                                                TransferStatus status,
                                                                                TransferType type) {

        return jpaRepository.findAllByExecutionTimeBeforeAndStatusAndType(now, status, type)
                .stream()
                .map(ScheduledTransferMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduledTransfer> findAllByRecurringDayAndTypeAndStatus(Integer recurringDay, TransferType type, TransferStatus status) {
        return jpaRepository.findAllByRecurringDayAndTypeAndStatus(recurringDay, type, status)
                .stream()
                .map(ScheduledTransferMapper::toDomain)
                .collect(Collectors.toList());
    }
}
