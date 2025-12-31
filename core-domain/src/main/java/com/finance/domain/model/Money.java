package com.finance.domain.model;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@ToString // 디버깅, 로깅 용도
public class Money {

    // 소수점 계산에 오차가 생기지 않도록 BigDecimal 사용
    private final BigDecimal amount; // 생성 후 값이 변경되지 않도록 보장

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    // 생성자에 검증 로직을 포함
    public Money(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        this.amount = amount;
    }

    public static Money of (long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    // 비즈니스 로직 (BigDecimal 사칙연산 활용)
    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isGreaterThan(Money zero) {
        return this.amount.compareTo(zero.amount) > 0;
    }

    // 수수료 계산 (1퍼센트)
    public Money calculateFee(double percentage) {
        // 퍼센트를 BigDecimal로 변환
        BigDecimal rate = BigDecimal.valueOf(percentage);

        // 100도 BigDecimal로 변환
        BigDecimal hund = BigDecimal.valueOf(100);

        // 수수료 계산 (퍼센트가 변경될 것을 대비해 고정 숫자로 두지 않음)
        return new Money(this.amount.multiply(rate).divide(hund));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        // compareTo == 0 이면 같은 것으로 취급 (소수점 무시)
        return amount.compareTo(money.amount) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(amount);
    }
}
