package com.finance.service.impl;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.Account;
import com.finance.domain.model.LimitPolicy;
import com.finance.domain.model.Money;
import com.finance.domain.model.Transaction;
import com.finance.domain.model.e.Currency;
import com.finance.domain.model.e.TransactionType;
import com.finance.domain.repository.AccountRepository;
import com.finance.domain.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionServiceImpl 단위 테스트")
class TransactionServiceImplUnitTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LimitPolicy limitPolicy;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        senderAccount = Account.builder()
                .id(1L).accountNumber("111111").balance(Money.of(10000L))
                .deleted(false).currency(Currency.KRW).build();

        receiverAccount = Account.builder()
                .id(2L).accountNumber("222222").balance(Money.of(5000L))
                .deleted(false).currency(Currency.KRW).build();
    }

    @Nested
    @DisplayName("deposit(입금) 메서드는")
    class Describe_deposit {

        @Test
        @DisplayName("성공: 유효한 계좌에 금액을 입금하고 거래 내역을 저장한다.")
        void success_deposit() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));

            // when
            transactionService.deposit(1L, 5000L);

            // then
            assertThat(senderAccount.getBalance()).isEqualTo(Money.of(15000L));
            verify(transactionRepository, times(1)).save(any(Transaction.class));
            verify(accountRepository, times(1)).save(senderAccount);
        }

        @Test
        @DisplayName("실패: 이미 삭제된 계좌에는 입금할 수 없다.")
        void fail_deposit_to_deleted_account() {
            // given
            Account deletedAccount = Account.builder().id(1L).deleted(true).build();
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(deletedAccount));

            // when & then
            assertThatThrownBy(() -> transactionService.deposit(1L, 1000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_DELETED);
        }
    }

    @Nested
    @DisplayName("withdraw(출금) 메서드는")
    class Describe_withdraw {

        @Test
        @DisplayName("성공: 잔액과 한도가 충분하면 출금에 성공한다.")
        void success_withdraw() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));
            given(transactionRepository.findDailySumByType(anyLong(), any(), eq(TransactionType.WITHDRAW)))
                    .willReturn(Money.ZERO);
            given(limitPolicy.getDailyWithdrawLimit()).willReturn(Money.of(50000L));

            // when
            transactionService.withdraw(1L, 3000L);

            // then
            assertThat(senderAccount.getBalance()).isEqualTo(Money.of(7000L));
            verify(accountRepository).save(senderAccount);
            verify(transactionRepository).save(any(Transaction.class));
        }

        @Test
        @DisplayName("실패: 잔액이 부족하면 예외가 발생한다.")
        void fail_insufficient_balance() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));
            given(transactionRepository.findDailySumByType(anyLong(), any(), any())).willReturn(Money.ZERO);

            // when & then
            assertThatThrownBy(() -> transactionService.withdraw(1L, 20000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
        }

        @Test
        @DisplayName("실패: 일일 출금 한도를 초과하면 예외가 발생한다.")
        void fail_withdraw_limit_exceeded() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));
            given(transactionRepository.findDailySumByType(anyLong(), any(), any())).willReturn(Money.of(45000L));
            given(limitPolicy.getDailyWithdrawLimit()).willReturn(Money.of(50000L));

            // when & then (누적 45000 + 신청 6000 > 50000)
            assertThatThrownBy(() -> transactionService.withdraw(1L, 6000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DAILY_WITHDRAW_LIMIT_EXCEEDED);
        }
    }

    @Nested
    @DisplayName("transfer(이체) 메서드는")
    class Describe_transfer {

        @Test
        @DisplayName("성공: 수수료를 포함한 금액이 차감되고 상대 계좌로 입금된다.")
        void success_transfer() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByAccountNumberWithLock("222222")).willReturn(Optional.of(receiverAccount));
            given(transactionRepository.findDailySumByType(anyLong(), any(), eq(TransactionType.TRANSFER_SEND)))
                    .willReturn(Money.ZERO);
            given(limitPolicy.getDailyTransferLimit()).willReturn(Money.of(100000L));

            // 5000원 이체 시 수수료 1% = 50원, 총 5050원 차감
            // when
            transactionService.transfer(1L, "222222", 5000L);

            // then
            assertThat(senderAccount.getBalance()).isEqualTo(Money.of(4950L)); // 10000 - 5050
            assertThat(receiverAccount.getBalance()).isEqualTo(Money.of(10000L)); // 5000 + 5000
            verify(transactionRepository, times(2)).save(any(Transaction.class)); // 송신/수신 내역 2개 저장
        }

        @Test
        @DisplayName("실패: 본인 계좌로의 이체는 불가능하다.")
        void fail_transfer_to_same_account() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByAccountNumberWithLock("111111")).willReturn(Optional.of(senderAccount));

            // when & then
            assertThatThrownBy(() -> transactionService.transfer(1L, "111111", 1000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRANSFER_TARGET_SAME);
        }

        @Test
        @DisplayName("실패: 수신자 계좌가 존재하지 않으면 예외가 발생한다.")
        void fail_receiver_not_found() {
            // given
            given(accountRepository.findByIdWithLock(1L)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByAccountNumberWithLock(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> transactionService.transfer(1L, "999-999", 1000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);
        }

        @Test
        @DisplayName("실패: 수수료 포함 금액이 잔액보다 많으면 이체할 수 없다.")
        void fail_transfer_insufficient_balance_with_fee() {
            // given
            Long senderId = 1L;
            String receiverAccountNumber = "222222";

            Account senderAccount = Account.builder()
                    .id(senderId)
                    .accountNumber("111111")
                    .balance(Money.of(1000L)) // 현재 잔액 1000원
                    .deleted(false)
                    .currency(Currency.KRW)
                    .build();

            Account receiverAccount = Account.builder()
                    .id(2L)
                    .accountNumber(receiverAccountNumber)
                    .balance(Money.of(5000L))
                    .deleted(false)
                    .currency(Currency.KRW)
                    .build();

            given(accountRepository.findByIdWithLock(senderId)).willReturn(Optional.of(senderAccount));
            given(accountRepository.findByAccountNumberWithLock(receiverAccountNumber)).willReturn(Optional.of(receiverAccount));

            // 한도 체크 통과를 위한 모킹
            given(transactionRepository.findDailySumByType(anyLong(), any(), any())).willReturn(Money.ZERO);
            given(limitPolicy.getDailyTransferLimit()).willReturn(Money.of(50000L));

            // when & then
            // 1000원 이체 시 수수료 1%(10원)가 붙어 총 1010원이 필요함 -> 잔액 1000원보다 많으므로 예외 발생
            assertThatThrownBy(() -> transactionService.transfer(senderId, receiverAccountNumber, 1000L))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_BALANCE);
        }
    }
}