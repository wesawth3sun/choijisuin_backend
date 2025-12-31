package com.finance.infra.entity;

import com.finance.domain.model.e.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId; // 대상 계좌 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount; // 거래 금액

    @Column(nullable = false)
    private BigDecimal fee; // 수수료

    @Column(nullable = false)
    private BigDecimal balanceAfterTransaction; // 거래 후 잔액 (조회용)

    private String targetAccountNumber; // 이체 시 상대방 계좌번호

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
