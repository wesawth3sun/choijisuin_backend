package com.finance.infra.repository;

import com.finance.domain.model.e.TransferStatus;
import com.finance.domain.model.e.TransferType;
import com.finance.infra.entity.ScheduledTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransferJpaRepository extends JpaRepository<ScheduledTransferEntity, Long> {
    List<ScheduledTransferEntity> findAllByExecutionTimeBeforeAndStatusAndType(LocalDateTime executionTimeBefore, TransferStatus status, TransferType type);

    List<ScheduledTransferEntity> findAllByRecurringDayAndTypeAndStatus(Integer recurringDay, TransferType type, TransferStatus status);
}
