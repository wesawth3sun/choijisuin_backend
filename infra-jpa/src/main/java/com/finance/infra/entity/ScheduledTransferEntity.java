package com.finance.infra.entity;

import com.finance.domain.model.TransferStatus;
import com.finance.domain.model.TransferType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 예약 이체 및 자동 이체 엔티티
 */

@Table(name = "scheduled_transfers")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduledTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long fromAccountId; // 출금 계좌 ID

    @Column(nullable = false)
    private String toAccountNumber; // 수취 계좌 번호

    @Column(nullable = false)
    private BigDecimal amount; // 이체 금액

    @Enumerated(EnumType.STRING)
    private TransferType type; // ONCE(예약), MONTHLY(자동)

    private LocalDateTime executionTime; // 실행 예정 시간 (예약 이체의 경우)
    private Integer recurringDay; // 매달 몇 일에 실행할지 (자동 이체의 경우)

    @Enumerated(EnumType.STRING)
    private TransferStatus status; // PENDING, COMPLETED, FAILED
}
