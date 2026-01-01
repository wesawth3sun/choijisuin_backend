package com.finance.domain.repository;


import com.finance.domain.model.ScheduledTransfer;
import com.finance.domain.model.e.TransferStatus;
import com.finance.domain.model.e.TransferType;

import java.time.LocalDateTime;
import java.util.List;

public interface ScheduledTransferRepository {
    void save(ScheduledTransfer domain);
    List<ScheduledTransfer> findAllByExecutionTimeBeforeAndStatusAndType(LocalDateTime now, TransferStatus transferStatus, TransferType transferType);
    List<ScheduledTransfer> findAllByRecurringDayAndTypeAndStatus(Integer recurringDay, TransferType type, TransferStatus status);
}
