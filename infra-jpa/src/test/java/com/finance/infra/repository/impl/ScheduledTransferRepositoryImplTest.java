package com.finance.infra.repository.impl;

import com.finance.domain.model.Money;
import com.finance.domain.model.ScheduledTransfer;
import com.finance.domain.model.e.TransferStatus;
import com.finance.domain.model.e.TransferType;
import com.finance.infra.entity.ScheduledTransferEntity;
import com.finance.infra.repository.ScheduledTransferJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScheduledTransferRepositoryImplTest {

    @Mock
    private ScheduledTransferJpaRepository jpaRepository;

    @InjectMocks
    private ScheduledTransferRepositoryImpl repository;

    @Test
    @DisplayName("도메인 객체를 저장하면 JPA 엔티티로 변환되어 저장되어야 한다")
    void save_Success() {
        // given
        ScheduledTransfer domain = ScheduledTransfer.create(
                1L, "12345", Money.of(1000L), TransferType.ONCE, LocalDateTime.now(), null
        );

        // when
        repository.save(domain);

        // then
        // jpaRepository의 save 메서드가 한 번 호출되었는지 확인
        verify(jpaRepository).save(any(ScheduledTransferEntity.class));
    }

    @Test
    @DisplayName("실행 시간과 상태, 타입으로 조건 조회 시 도메인 리스트로 올바르게 변환되어야 한다")
    void findAllByExecutionTimeBefore_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = BigDecimal.valueOf(1000L);
        ScheduledTransferEntity entity = ScheduledTransferEntity.builder()
                .id(1L)
                .fromAccountId(1L)
                .toAccountNumber("12345")
                .amount(amount)
                .status(TransferStatus.PENDING)
                .type(TransferType.ONCE)
                .executionTime(now.minusMinutes(10))
                .build();

        given(jpaRepository.findAllByExecutionTimeBeforeAndStatusAndType(any(), any(), any()))
                .willReturn(List.of(entity));

        // when
        List<ScheduledTransfer> results = repository.findAllByExecutionTimeBeforeAndStatusAndType(
                now, TransferStatus.PENDING, TransferType.ONCE
        );

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
        assertThat(results.get(0).getAmount().getAmount()).isEqualTo(BigDecimal.valueOf(1000L));
        verify(jpaRepository).findAllByExecutionTimeBeforeAndStatusAndType(now, TransferStatus.PENDING, TransferType.ONCE);
    }

    @Test
    @DisplayName("정기 실행일 조건 조회 시 결과가 도메인 모델로 매핑되어야 한다")
    void findAllByRecurringDay_Success() {
        // given
        int recurringDay = 25;
        BigDecimal amount = BigDecimal.valueOf(5000L);
        ScheduledTransferEntity entity = ScheduledTransferEntity.builder()
                .id(10L)
                .recurringDay(recurringDay)
                .status(TransferStatus.PENDING)
                .type(TransferType.MONTHLY)
                .amount(amount)
                .build();

        given(jpaRepository.findAllByRecurringDayAndTypeAndStatus(recurringDay, TransferType.MONTHLY, TransferStatus.PENDING))
                .willReturn(List.of(entity));

        // when
        List<ScheduledTransfer> results = repository.findAllByRecurringDayAndTypeAndStatus(
                recurringDay, TransferType.MONTHLY, TransferStatus.PENDING
        );

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRecurringDay()).isEqualTo(recurringDay);
        assertThat(results.get(0).getType()).isEqualTo(TransferType.MONTHLY);
        verify(jpaRepository).findAllByRecurringDayAndTypeAndStatus(recurringDay, TransferType.MONTHLY, TransferStatus.PENDING);
    }
}