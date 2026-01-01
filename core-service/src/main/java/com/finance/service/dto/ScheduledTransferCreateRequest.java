package com.finance.service.dto;

import com.finance.domain.model.TransferType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
public class ScheduledTransferCreateRequest {

    private Long fromAccountId;
    private String toAccountNumber;
    private Long amount;
    private TransferType type;
    private LocalDateTime executionTime; // 예약 이체시 사용 (서비스 로직에서 추가 검증)
    private Integer recurringDay; // 값이 비어있어도 되지만, 있다면 1~31 사이여야 함
}
