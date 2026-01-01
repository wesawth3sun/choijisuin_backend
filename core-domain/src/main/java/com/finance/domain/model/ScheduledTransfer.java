package com.finance.domain.model;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.e.TransferStatus;
import com.finance.domain.model.e.TransferType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class ScheduledTransfer {

    private final Long id;
    private final Long fromAccountId; // 출금 계좌 ID
    private final String toAccountNumber; // 수취 계좌 번호
    private final Money amount; // 이체 금액
    private TransferType type; // ONCE(예약), MONTHLY(자동)
    private final LocalDateTime executionTime; // 실행 예정 시간 (예약 이체의 경우)
    private final Integer recurringDay; // 매달 몇 일에 실행할지 (자동 이체의 경우)
    private TransferStatus status; // PENDING, COMPLETED, FAILED

    /**
     * 새로운 이체 스케줄을 생성하는 정적 팩토리 메서드입니다.
     * 모든 이체 스케줄은 생성 시 'PENDING(대기)' 상태로 초기화됩니다.
     *
     * @param fromAccountId   출금 계좌 ID
     * @param toAccountNumber 수취 계좌 번호
     * @param amount          이체 금액 객체
     * @param type            이체 유형 (ONCE 또는 MONTHLY)
     * @param executionTime   실행 예정 시간 (단건 이체 시 사용)
     * @param recurringDay    매달 실행 날짜 (정기 이체 시 사용)
     * @return 초기화된 ScheduledTransfer 도메인 객체
     */
    public static ScheduledTransfer create(Long fromAccountId, String toAccountNumber,
                                           Money amount, TransferType type,
                                           LocalDateTime executionTime, Integer recurringDay) {
        return ScheduledTransfer.builder()
                .fromAccountId(fromAccountId)
                .toAccountNumber(toAccountNumber)
                .amount(amount)
                .type(type)
                .executionTime(executionTime) // null 가능
                .recurringDay(recurringDay) // null 가능
                .status(TransferStatus.PENDING)
                .build();
    }

    /**
     * 이체 스케줄의 상태를 변경합니다.
     * 동일한 상태로의 변경을 방지하고, 완료된 이체가 다시 대기 상태로 돌아가는 것을 금지하는 규칙을 포함합니다.
     *
     * @param status 변경하고자 하는 새로운 상태 (COMPLETED, FAILED 등)
     * @throws BusinessException 기존 상태와 동일한 경우 (INVALID_TRANSFER_STATUS_CHANGE)
     * @throws BusinessException 완료된 이체를 대기로 돌리려는 경우 (TRANSFER_CANNOT_BE_RESUMED)
     */
    public void changeStatus(TransferStatus status) {
        // 1. 현재 상태와 변경하려는 상태가 동일한지 검증
        if (this.status.equals(status)) {
            throw new BusinessException(ErrorCode.INVALID_TRANSFER_STATUS_CHANGE);
        }

        // 2. 비즈니스 규칙: 이미 완료(COMPLETED)된 이체는 다시 대기(PENDING)로 변경 불가
        if (this.status.equals(TransferStatus.COMPLETED) && status.equals(TransferStatus.PENDING)) {
            throw new BusinessException(ErrorCode.TRANSFER_CANNOT_BE_RESUMED);
        }

        this.status = status; // 상태 변경 적용
    }
}
