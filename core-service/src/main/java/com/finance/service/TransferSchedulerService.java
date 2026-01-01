package com.finance.service;


import com.finance.service.dto.ReservationTimeResponse;

import java.time.LocalDateTime;

public interface TransferSchedulerService {
    ReservationTimeResponse getAvailableReservationTimes();
    void saveScheduledTransfer(Long fromAccountId, String toAccountNumber, Long amount, String type, LocalDateTime executionTime, Integer recurringDay);
}
