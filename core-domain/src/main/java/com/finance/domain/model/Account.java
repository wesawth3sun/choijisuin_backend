package com.finance.domain.model;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.e.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
@AllArgsConstructor
public class Account {
    private final Long id;
    private final String accountNumber;
    private Money balance;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final Currency currency; // 화폐 단위

    public static Account create(String accountNumber, Currency currency) {
        return Account.builder()
                .accountNumber(accountNumber)
                .balance(Money.ZERO) // 잔고는 0원으로 초기화
                .deleted(false) // 계좌는 활성화 상태로 초기화
                .createdAt(LocalDateTime.now())
                .currency(currency)
                .build();
    }

    /**
     * 입금 로직
     * @param amount 입금 금액
     */
    public void deposit(Money amount) {
        this.balance = this.balance.add(amount);
    }

    /**
     * 출금 로직
     * @param amount 출금 금액
     * @param dailyUsage 일 출금 금액 총합
     * @param limitPolicy 한도 정책
     */
    public void withdraw(Money amount, Money dailyUsage, LimitPolicy limitPolicy) {
        if (this.balance.isLessThan(amount)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE); // 잔액 부족
        }

        // 정책에서 한도 금액 가져오기
        Money limit = limitPolicy.getDailyWithdrawLimit();

        // 일 한도 체크 (이체할 금액 + 이전 출금 금액 > 일 한도)
        if (!dailyUsage.add(amount).isLessThan(limit)) {
            throw new BusinessException(ErrorCode.DAILY_WITHDRAW_LIMIT_EXCEEDED);
        }

        this.balance = this.balance.subtract(amount);
    }

    /**
     * 이체 로직
     * @param targetAccount 이체할 계좌
     * @param amount 이체 금액
     * @param dailyUsage 일 이체 금액 총합
     * @param limitPolicy 일 이체 한도
     */
    public void transfer(Account targetAccount, Money amount, Money dailyUsage, LimitPolicy limitPolicy) {
        Money fee = amount.calculateFee(1.0); // 수수료 계산
        Money totalAmount = amount.add(fee);// 수수료 포함한 금액 계산

        Money limit = limitPolicy.getDailyTransferLimit(); // 이체 한도 금액

        // 이체 한도 체크
        if (!dailyUsage.add(totalAmount).isLessThan(limit)) {
            throw new BusinessException(ErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);
        }

        // 출금 로직 실행
        if (this.balance.isLessThan(totalAmount)) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        this.balance = this.balance.subtract(totalAmount);

        // 입금 로직 실행 (상대방 계좌에)
        targetAccount.deposit(amount);
    }

    /**
     * 계좌 삭제 로직
     */
    public Account delete() {
        // 잔액이 있으면 계좌를 삭제할 수 없음
        if (this.balance.isGreaterThan(Money.ZERO)) {
            throw new BusinessException(ErrorCode.ACCOUNT_HAS_BALANCE);
        }

        this.deleted = true;

        return this;
    }
}
