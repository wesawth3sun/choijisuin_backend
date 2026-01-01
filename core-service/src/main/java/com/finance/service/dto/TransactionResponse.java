package com.finance.service.dto;

import com.finance.domain.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TransactionResponse {

    private final BigDecimal amount; // 거래 금액
    private final BigDecimal fee; // 수수료
    private final String type; // 거래 유형
    private final BigDecimal balanceAfterTransaction; // 거래 후 잔액
    private final LocalDateTime createdAt; // 거래 일시
    private final String targetAccountNumber; // 거래 상대방 계좌 번호

    public static TransactionResponse of(Transaction transaction) {
        return new TransactionResponse(
                transaction.getAmount().getAmount(),
                transaction.getFee().getAmount(),
                transaction.getType().getDescription(),
                transaction.getBalanceAfterTransaction().getAmount(),
                transaction.getCreatedAt(),
                transaction.getTargetAccountNumber()
        );
    }
}
