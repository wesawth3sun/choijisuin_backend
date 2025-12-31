package com.finance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Money 도메인 테스트")
class MoneyTest {

    @Test
    @DisplayName("1% 수수료가 정상적으로 계산되어야 한다")
    void calculateFee_Success() {
        // given
        Money amount = Money.of(10000L); // 10,000원
        double feePercentage = 1.0;

        // when
        Money fee = amount.calculateFee(feePercentage);

        // then
        // 10,000 * (1 / 100) = 100
        assertThat(fee).isEqualTo(Money.of(100L));
    }

    @Test
    @DisplayName("수수료 계산 시 원본 Money 객체의 금액은 변하지 않아야 한다 (불변성 확인)")
    void calculateFee_Immutability() {
        // given
        Money originalAmount = Money.of(1000L);

        // when
        originalAmount.calculateFee(1.0);

        // then
        assertThat(originalAmount.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000L));
    }

    /**
     * 수수료가 변경될 경우를 대비
     */
    @ParameterizedTest
    @DisplayName("다양한 금액과 요율에 대해 수수료가 정확히 계산되어야 한다")
    @CsvSource({
            "1000, 1.0, 10",      // 1000원의 1%는 10원
            "50000, 1.5, 750",    // 50000원의 1.5%는 750원
            "100, 10.0, 10",      // 100원의 10%는 10원
            "0, 1.0, 0"           // 0원의 수수료는 0원
    })
    void calculateFee_VariousScenarios(long inputAmount, double percentage, long expectedFee) {
        // given
        Money money = Money.of(inputAmount);

        // when
        Money resultFee = money.calculateFee(percentage);

        // then
        assertThat(resultFee).isEqualTo(Money.of(expectedFee));
    }
}