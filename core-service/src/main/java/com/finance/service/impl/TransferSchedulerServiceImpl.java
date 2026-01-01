package com.finance.service.impl;

import com.finance.domain.model.e.TransferType;
import com.finance.service.TransferSchedulerService;
import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.Account;
import com.finance.domain.model.Money;
import com.finance.domain.model.ScheduledTransfer;
import com.finance.domain.repository.AccountRepository;
import com.finance.domain.repository.ScheduledTransferRepository;
import com.finance.service.dto.ReservationTimeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferSchedulerServiceImpl implements TransferSchedulerService {

    private final ScheduledTransferRepository scheduledTransferRepository;
    private final AccountRepository accountRepository;

    /**
     * 사용자가 예약 이체를 설정할 때 선택 가능한 시간 범위와 가이드를 조회합니다.
     * @return minAvailableTime(최소 시간), maxAvailableTime(최대 시간), 안내 메시지를 포함한 응답 객체
     */
    @Override
    public ReservationTimeResponse getAvailableReservationTimes() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 현재 분(minute) 추출
        int minutes = now.getMinute();

        // 2. 무조건 10분 단위로 올림 처리
        // 11~19분 -> 20분, 21~29분 -> 30분이 되며 만약 현재가 정확히 10분, 20분 단위라면 그대로 유지
        int roundedMinutes = ((minutes + 9) / 10) * 10;

        // 3. 올림된 시간을 기준으로 베이스 시간 설정
        // plusMinutes를 사용하므로 50분에서 올림되어 60분이 되어도 다음 시간(Hour)으로 자동 계산 가능
        LocalDateTime baseTime = now.withMinute(0).withSecond(0).withNano(0)
                .plusMinutes(roundedMinutes);

        // 4. 최소 예약 가능 시간: 10분 단위 올림 기준 시간 + 30분
        LocalDateTime minTime = baseTime.plusMinutes(30);

        // 5. 최대 예약 가능 시간: 현재 시간 기준 30일 후의 자정 직전까지
        LocalDateTime maxTime = now.plusDays(30).withHour(23).withMinute(50).withSecond(0).withNano(0);

        return new ReservationTimeResponse(
                minTime,
                maxTime,
                String.format("현재 시간(%02d분) 기준 %02d분으로 올림하여, 30분 뒤인 %s부터 10분 단위로 예약 가능합니다. (최대 30일 후까지)",
                        minutes, baseTime.getMinute(), minTime.toLocalTime().toString())
        );
    }

    /**
     * 새로운 예약 또는 자동 이체 스케줄을 검증하고 저장합니다.
     *
     * @param request 이체 예약에 필요한 정보 (계좌 ID, 번호, 금액, 타입, 시간 등)
     * @throws BusinessException 계좌를 찾을 수 없는 경우 (ACCOUNT_NOT_FOUND)
     * @throws BusinessException 계좌가 삭제된 경우 (ACCOUNT_ALREADY_DELETED)
     * @throws BusinessException 예약 시간이 10분 단위가 아닌 경우 (INVALID_TRANSFER_TIME_UNIT)
     */
    @Transactional
    @Override
    public void saveScheduledTransfer(Long fromAccountId, String toAccountNumber, Long amount, String type, LocalDateTime executionTime, Integer recurringDay) {
        log.info("[SCHEDULED TRANSFER] 예약 이체 요청 - 계좌 ID: {}, 계좌 번호: {}, 금액: {}, 타입: {}, 시간: {}, 일자: {}",
                fromAccountId, toAccountNumber, amount, type, executionTime, recurringDay);

        // 1. 계좌 번호 조회
        Account fromAccount = accountRepository.findById(fromAccountId).orElseThrow(
                () -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        );

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber).orElseThrow(
                () -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)
        );

        // 2. 계좌 상태 검증 (삭제된 계좌를 통한 거래 방지)
        if (toAccount.isDeleted() || fromAccount.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        // 3. 시간 단위 유효성 검사 (스케줄러는 10분 단위로 작동하므로 정합성 유지)
        TransferType transferType = TransferType.toTransferType(type);
        if (transferType == TransferType.ONCE) {
            if (executionTime.getMinute() % 10 != 0) {
                throw new BusinessException(ErrorCode.INVALID_TRANSFER_TIME_UNIT);
            }
        }

        // 4. 도메인 객체 생성
        ScheduledTransfer scheduledTransfer = ScheduledTransfer.create(
                fromAccountId,
                toAccountNumber,
                new Money(BigDecimal.valueOf(amount)),
                transferType,
                executionTime,
                recurringDay
        );

        // 4. 저장
        scheduledTransferRepository.save(scheduledTransfer);
        log.info("[SCHEDULED TRANSFER] 예약 이체 저장 완료 - ID: {}", scheduledTransfer.getId());
    }
}
