package com.finance.service.impl;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.Account;
import com.finance.domain.model.Money;
import com.finance.domain.model.e.Currency;
import com.finance.domain.repository.AccountRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 환경 설정
@DisplayName("AccountService 단위 테스트 (Mockito)")
class AccountServiceImplUnitTest {

    @Mock
    private AccountRepository accountRepository; // 가짜 객체 생성

    @InjectMocks
    private AccountServiceImpl accountService; // 가짜 객체를 주입받는 서비스 인스턴스

    @Nested
    @DisplayName("계좌 등록 테스트")
    class RegisterAccount {

        @Test
        @DisplayName("성공: 중복되지 않는 계좌 번호면 저장 로직을 호출한다")
        void register_success() {
            // given
            String accountNumber = "88889999";
            String currency = "KRW";

            // 1. findBy가 아니라 existsBy로 변경 (결과는 false로 설정)
            given(accountRepository.existsByAccountNumber(accountNumber)).willReturn(false);

            // 2. save 호출 시 반환될 ID 설정 (save가 객체를 반환한다면 mockAccount를, ID를 반환한다면 1L을 반환하게 타입 확인)
            Account mockAccount = Account.builder().id(1L).accountNumber(accountNumber).build();
            given(accountRepository.save(any(Account.class))).willReturn(mockAccount);

            // when
            Long savedId = accountService.registerAccount(accountNumber, currency);

            // then
            assertThat(savedId).isEqualTo(1L);

            // 3. 검증부도 existsBy로 변경
            verify(accountRepository).existsByAccountNumber(accountNumber);
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("실패: 중복된 계좌 번호가 존재하면 예외가 발생한다")
        void register_fail_duplicate() {
            // given
            String accountNumber = "11112222";
            String currency = "KRW";

            // 서비스 로직에서 사용 중인 existsByAccountNumber가 true를 반환하도록 Mock 설정
            given(accountRepository.existsByAccountNumber(accountNumber)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> accountService.registerAccount(accountNumber, currency))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.ACCOUNT_NUMBER_DUPLICATED.getMessageKey());

            // 실제로 existsBy... 호출이 일어났는지 검증
            verify(accountRepository).existsByAccountNumber(accountNumber);
        }
    }

    @Nested
    @DisplayName("계좌 삭제 요청 시")
    class DeleteAccountTest {

        @Test
        @DisplayName("성공: 잔액이 0원이고 존재하는 계좌라면 삭제(Soft Delete) 상태로 변경된다.")
        void success_delete_account() {
            // given
            Long accountId = 1L;
            Account account = Account.create("123456", Currency.KRW); // 초기 잔액 0원

            given(accountRepository.findByIdWithLock(accountId))
                    .willReturn(Optional.of(account));

            // save 호출 시 전달된 객체를 그대로 반환하도록 설정
            given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            accountService.deleteAccount(accountId);

            // then
            assertThat(account.isDeleted()).isTrue();
            verify(accountRepository, times(1)).findByIdWithLock(accountId);
            verify(accountRepository, times(1)).save(account);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 계좌 ID인 경우 예외가 발생한다.")
        void fail_account_not_found() {
            // given
            Long accountId = 999L;
            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);

            verify(accountRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 이미 삭제된 계좌인 경우 예외가 발생한다.")
        void fail_already_deleted() {
            // given
            Long accountId = 1L;
            Account account = Account.builder()
                    .id(accountId)
                    .accountNumber("123456")
                    .deleted(true) // 이미 삭제된 상태
                    .build();

            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(account));

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        @Test
        @DisplayName("실패: 계좌에 잔액이 남아있는 경우 삭제할 수 없다.")
        void fail_has_balance() {
            // given
            Long accountId = 1L;
            Account account = Account.create("123456", Currency.KRW);
            account.deposit(Money.of(1000)); // 잔액 1000원 추가

            given(accountRepository.findByIdWithLock(accountId)).willReturn(Optional.of(account));

            // when & then
            assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_HAS_BALANCE);

            assertThat(account.isDeleted()).isFalse(); // 상태가 변하지 않아야 함
            verify(accountRepository, never()).save(any());
        }
    }
}