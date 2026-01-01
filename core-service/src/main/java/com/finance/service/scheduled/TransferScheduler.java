package com.finance.service.scheduled;

import com.finance.service.TransactionService;
import com.finance.domain.model.ScheduledTransfer;
import com.finance.domain.model.e.TransferStatus;
import com.finance.domain.model.e.TransferType;
import com.finance.domain.repository.ScheduledTransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 분산 서비스에서는 쿼츠 스케줄러를 사용,
 * 대규모 서비스에서는 배치 처리를 활용하는 것으로 확장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransferScheduler {

    private final TransactionService transactionService;
    private final ScheduledTransferRepository repository;

    /**
     * [메서드 1] 예약 이체 (10분마다 실행)
     * executionTime이 현재 시간보다 이전이고 아직 PENDING 상태인 건을 처리합니다.
     */
    @Scheduled(cron = "0 0/10 * * * *") // 매 10분 0초에 실행 (yml 에서 설정해서 사용할 수도 있음)
    @Transactional
    public void executeReservedTransfers() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[SCHEDULED] 예약 이체 프로세스 시작: {}", now);

        // 현재 시간 이전에 실행되었어야 할 PENDING 상태의 예약(ONCE) 건 조회
        List<ScheduledTransfer> targets = repository.findAllByExecutionTimeBeforeAndStatusAndType(
                now, TransferStatus.PENDING, TransferType.ONCE);

        for (ScheduledTransfer target : targets) {
            processTransfer(target);
        }

        log.info("[SCHEDULED] 예약 이체 프로세스 종료: {} 건 업데이트", targets.size());
    }

    /**
     * [메서드 2] 자동 이체 (매일 새벽 1시 실행)
     * recurringDay가 오늘 날짜와 일치하는 정기 이체 건을 처리합니다.
     */
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 0분 0초에 실행
    @Transactional
    public void executeMonthlyTransfers() {
        int todayDay = LocalDate.now().getDayOfMonth();
        log.info("[SCHEDULED] 자동 이체 프로세스 시작 (매달 {}일 대상)", todayDay);

        // 오늘이 정기 이체일인 MONTHLY 건 조회
        List<ScheduledTransfer> targets = repository.findAllByRecurringDayAndTypeAndStatus(
                todayDay,
                TransferType.MONTHLY,
                TransferStatus.PENDING
        );

        for (ScheduledTransfer target : targets) {
            processTransfer(target);
        }

        log.info("[SCHEDULED] 자동 이체 프로세스 종료 ({} 건 업데이트)", targets.size());
    }

    /**
     * 실제 이체 로직을 실행하고 결과를 업데이트하는 공통 메서드
     * + 자동 이체 또한 일 이체 한도에 포함된다
     */
    private void processTransfer(ScheduledTransfer transfer) {
        try {
            log.info("[SCHEDULED] 이체 시도 - From: {}, To: {}",
                    transfer.getFromAccountId(), transfer.getToAccountNumber());

            // 기존 TransactionService의 transfer 로직 재사용
            transactionService.transfer(
                    transfer.getFromAccountId(),
                    transfer.getToAccountNumber(),
                    transfer.getAmount().getAmount().longValue()
            );

            // 성공 시 상태 업데이트
            if (transfer.getType() == TransferType.ONCE) {
                transfer.changeStatus(TransferStatus.COMPLETED);
                repository.save(transfer);
            } else if (transfer.getType() == TransferType.MONTHLY) {
                transfer.changeStatus(TransferStatus.COMPLETED);
                repository.save(transfer);
                createNewDomain(transfer);
            }
        } catch (Exception e) {
            log.error("[SCHEDULED] 이체 실패 - ID: {}, 사유: {}", transfer.getId(), e.getMessage());
            transfer.changeStatus(TransferStatus.FAILED); // 실패 상태로 변경
            repository.save(transfer);

            // 실패 시 사용자에게 푸시 알림 전송 등
        }
    }

    private void createNewDomain(ScheduledTransfer transfer) {
        ScheduledTransfer newScheduled = ScheduledTransfer.create(
                transfer.getFromAccountId(),
                transfer.getToAccountNumber(),
                transfer.getAmount(),
                TransferType.MONTHLY,
                null,
                transfer.getRecurringDay()
        );
        repository.save(newScheduled);
    }
}
