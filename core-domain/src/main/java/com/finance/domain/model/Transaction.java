package com.finance.domain.model;

import com.finance.domain.model.e.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 불변성 유지
 */
@Getter
@ToString
public class Transaction {

    private final Long id;
    private final Long accountId;
    private final TransactionType type;
    private final Money amount;
    private final Money fee;
    private final Money balanceAfterTransaction;
    private final String targetAccountNumber;
    private final LocalDateTime createdAt;

    @Builder
    public Transaction(Long id, Long accountId, TransactionType type, Money amount,
                       Money fee, Money balanceAfterTransaction, String targetAccountNumber,
                       LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.fee = fee;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.targetAccountNumber = targetAccountNumber;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    /**
     * 입금 거래 내역 생성 팩토리 메서드
     * @param account 계좌
     * @param amount 금액
     * @return Transaction 거래 내역
     */
    public static Transaction createDeposit(Account account, Money amount) {
        return Transaction.builder()
                .accountId(account.getId())
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .fee(Money.ZERO) // 입금 시 수수료는 0원
                .targetAccountNumber(account.getAccountNumber())
                .balanceAfterTransaction(account.getBalance()) // 내 계좌
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 출금 거래 내역 생성 팩토리 메서드
     * @param account 출금한 계좌
     * @param amount 출금 금액
     */
    public static Transaction createWithdraw(Account account, Money amount) {
        return Transaction.builder()
                .accountId(account.getId())
                .type(TransactionType.WITHDRAW)
                .amount(amount)
                .fee(Money.ZERO) // 출금 시 수수료는 0원
                .balanceAfterTransaction(account.getBalance()) // 출금 후 잔액
                .targetAccountNumber(account.getAccountNumber()) // 내 계좌
                .build();
    }

    /**
     * 이체(송금) 거래 내역 생성 팩토리 메서드
     * @param account 출금 계좌
     * @param target 입금 계좌
     * @param amount 금액
     * @param fee 수수료
     * @return Transaction 거래 내역
     */
    public static Transaction createTransferSend(Account senderAccount, String targetAccountNumber, Money amount, Money fee) {
        return Transaction.builder()
                .accountId(senderAccount.getId())
                .type(TransactionType.TRANSFER_SEND) // 이체(송금) 타입
                .amount(amount)
                .fee(fee)
                .targetAccountNumber(targetAccountNumber)
                .balanceAfterTransaction(senderAccount.getBalance()) // 출금 후 잔액
                .build();
    }
    /**
     * 이체(수신) 거래 내역 생성 팩토리 메서드
     * @param account 수취인 계좌 (입금이 완료된 상태)
     * @param senderAccountNumber 보낸 사람의 계좌 번호
     * @param amount 입금된 금액
     */
    public static Transaction createTransferReceive(Account account, String senderAccountNumber, Money amount) {
        return Transaction.builder()
                .accountId(account.getId()) // 수취인 계좌 ID
                .type(TransactionType.TRANSFER_RECEIVE) // 이체(입금) 타입
                .amount(amount) // 입금된 금액
                .fee(Money.ZERO) // 받는 사람은 수수료가 0원
                .targetAccountNumber(senderAccountNumber) // 보낸 사람 계좌 번호
                .balanceAfterTransaction(account.getBalance()) // 입금 후 잔액
                .build();
    }
}
