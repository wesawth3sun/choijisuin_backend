package com.finance.service;


import com.finance.domain.model.TransferType;
import com.finance.service.dto.ReservationTimeResponse;

import java.time.LocalDateTime;

public interface TransferSchedulerService {
    ReservationTimeResponse getAvailableReservationTimes();
    void saveScheduledTransfer(Long fromAccountId, String toAccountNumber, Long amount, TransferType type, LocalDateTime executionTime, Integer recurringDay);
}
