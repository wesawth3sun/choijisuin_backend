package com.finance.domain.model;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.e.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account 도메인 테스트")
class AccountTest {

    private Account createAccount(long id, String accountNumber, long balanceAmount) {
        return new Account(id,
                accountNumber,
                Money.of(balanceAmount),
                false,
                LocalDateTime.now(),
                Currency.KRW
        );
    }

    private Money money(long amount) {
        return new Money(BigDecimal.valueOf(amount));
    }

    private LimitPolicy createLimitPolicy(long withdrawLimit, long transferLimit) {
        return new LimitPolicy() {
            @Override
            public Money getDailyWithdrawLimit() {
                return money(withdrawLimit);
            }

            @Override
            public Money getDailyTransferLimit() {
                return money(transferLimit);
            }
        };
    }

    @Nested
    @DisplayName("입금(deposit) 테스트")
    class DepositTest {

        @Test
        @DisplayName("정상적으로 금액이 입금되어야 한다")
        void deposit_success() {
            // given
            Account account = createAccount(1L, "1234567890123456", 1000);
            Money depositAmount = money(500);

            // when
            account.deposit(depositAmount);

            // then
            assertThat(account.getBalance()).isEqualTo(money(1500));
        }
    }

    @Nested
    @DisplayName("출금(withdraw) 테스트")
    class WithdrawTest {

        @Test
        @DisplayName("잔액과 한도가 충분하면 출금에 성공해야 한다")
        void withdraw_success() {
            // given
            Account account = createAccount(1L, "1234567890123456", 10000);
            Money withdrawAmount = money(3000);
            Money dailyUsage = money(1000); // 이미 1000원 씀
            LimitPolicy policy = createLimitPolicy(5000, 5000); // 출금 한도 5000원 설정

            // when
            account.withdraw(withdrawAmount, dailyUsage, policy);

            // then
            assertThat(account.getBalance()).isEqualTo(money(7000));
        }

        @Test
        @DisplayName("잔액이 부족하면 예외가 발생해야 한다 (INSUFFICIENT_BALANCE)")
        void withdraw_fail_insufficient_balance() {
            // given
            Account account = createAccount(1L, "1234567890123456", 1000); // 잔액 1000원
            Money withdrawAmount = money(2000);    // 2000원 출금 시도
            Money dailyUsage = Money.ZERO;
            LimitPolicy policy = createLimitPolicy(100000, 100000);

            // when & then
            assertThatThrownBy(() -> account.withdraw(withdrawAmount, dailyUsage, policy))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("일일 한도를 초과하면 예외가 발생해야 한다 (DAILY_WITHDRAW_LIMIT_EXCEEDED)")
        void withdraw_fail_limit_exceeded() {
            // given
            Account account = createAccount(1L, "1234567890123456", 10000); // 돈은 충분함
            Money withdrawAmount = money(4000);
            Money dailyUsage = money(2000); // 이미 2000원 씀 (합계 6000원 예정)
            LimitPolicy policy = createLimitPolicy(5000, 10000);

            // when & then
            assertThatThrownBy(() -> account.withdraw(withdrawAmount, dailyUsage, policy))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_WITHDRAW_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("이체 성공 케이스")
    class SuccessTest {

        @Test
        @DisplayName("정상적으로 이체가 완료되어야 한다 (수수료 1% 포함)")
        void transfer_success() {
            // given
            // 내 잔고: 10,000원
            Account myAccount = createAccount(1L, "1234567890123456", 10_000);
            // 상대 잔고: 0원
            Account targetAccount = createAccount(2L, "1234567890124557", 0);

            // 1,000원 이체 시도 (수수료 1% = 10원, 총 1,010원 차감 예정)
            Money transferAmount = Money.of(1_000);
            Money dailyUsage = Money.ZERO; // 오늘 이체한 돈의 총합 0원
            LimitPolicy policy = createLimitPolicy(10000, 10000);

            // when
            myAccount.transfer(targetAccount, transferAmount, dailyUsage, policy);

            // then
            // 내 통장: 10,000 - 1,010 = 8,990원 남아야 함
            assertThat(myAccount.getBalance()).isEqualTo(Money.of(8_990));

            // 상대 통장: 0 + 1,000 = 1,000원 들어와야 함
            assertThat(targetAccount.getBalance()).isEqualTo(Money.of(1_000));
        }
    }

    @Nested
    @DisplayName("이체 실패 케이스")
    class FailTest {

        @Test
        @DisplayName("잔액이 부족하면 이체할 수 없다 (수수료 포함 금액 기준)")
        void fail_insufficient_balance() {
            // given
            // 내 잔고: 1,000원
            Account myAccount = createAccount(1L, "1234567890123456", 1_000); // 잔고 1,000원
            // 상대 잔고: 0원
            Account targetAccount = createAccount(2L, "1234567890124557", 0);
            LimitPolicy policy = createLimitPolicy(10000, 10000);

            // 1,000원 이체 시도 (수수료 10원 포함 총 1,010원 필요 -> 잔액 부족!)
            Money transferAmount = Money.of(1_000);
            Money dailyUsage = Money.ZERO;


            // when & then
            assertThatThrownBy(() ->
                    myAccount.transfer(targetAccount, transferAmount, dailyUsage, policy)
            )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("일일 이체 한도(300만원)를 초과하면 이체할 수 없다")
        void fail_limit_exceeded() {
            // given
            Account myAccount = createAccount(1L, "1234567890123456", 5_000_000); // 돈은 많음
            Account targetAccount = createAccount(2L, "1234567890124775", 0);

            // 100만원 이체 시도
            Money transferAmount = Money.of(1_000_000);
            // 이미 250만원을 썼음 (총합 350만원 > 300만원 한도 초과)
            Money dailyUsage = Money.of(2_500_000);

            LimitPolicy policy = createLimitPolicy(1_000_000, 3_000_000);

            // when & then
            assertThatThrownBy(() ->
                    myAccount.transfer(targetAccount, transferAmount, dailyUsage, policy)
            )
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_TRANSFER_LIMIT_EXCEEDED);
        }
    }
}
