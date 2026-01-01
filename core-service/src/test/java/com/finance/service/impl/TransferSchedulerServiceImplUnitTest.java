package com.finance.service.impl;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.Account;
import com.finance.domain.model.Money;
import com.finance.domain.model.ScheduledTransfer;
import com.finance.domain.model.e.Currency;
import com.finance.domain.model.e.TransferType;
import com.finance.domain.repository.AccountRepository;
import com.finance.domain.repository.ScheduledTransferRepository;
import com.finance.service.dto.ReservationTimeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferSchedulerServiceImpl 단위 테스트")
class TransferSchedulerServiceImplUnitTest {
    @InjectMocks
    private TransferSchedulerServiceImpl transferSchedulerService;

    @Mock
    private ScheduledTransferRepository scheduledTransferRepository;

    @Mock
    private AccountRepository accountRepository;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = Account.builder()
                .id(1L).accountNumber("111-111").balance(Money.of(10000L))
                .deleted(false).currency(Currency.KRW).build();

        toAccount = Account.builder()
                .id(2L).accountNumber("222-222").balance(Money.of(5000L))
                .deleted(false).currency(Currency.KRW).build();
    }

    @Nested
    class Describe_getAvailableReservationTimes {

        @Test
        @DisplayName("성공: 현재 시간 기준으로 10분 단위 올림된 최소/최대 예약 가능 시간을 반환한다.")
        void it_returns_available_reservation_time_range() {
            // when
            ReservationTimeResponse response = transferSchedulerService.getAvailableReservationTimes();

            // then
            assertThat(response.getMinAvailableTime()).isAfter(LocalDateTime.now());
            assertThat(response.getMaxAvailableTime()).isAfter(response.getMinAvailableTime());

            // 10분 단위 검증 (분 단위가 10으로 나누어 떨어져야 함)
            assertThat(response.getMinAvailableTime().getMinute() % 10).isEqualTo(0);
        }
    }

    @Nested
    class Describe_saveScheduledTransfer {

        private final Long amount = 1000L;
        private final LocalDateTime validTime = LocalDateTime.now().plusDays(1).withMinute(20).withSecond(0).withNano(0);

        @Test
        @DisplayName("성공: 모든 조건이 유효하면 예약 이체를 저장한다.")
        void success_save_scheduled_transfer() {
            // given
            given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("222222")).willReturn(Optional.of(toAccount));

            // when
            transferSchedulerService.saveScheduledTransfer(1L, "222222", amount, "ONCE", validTime, null);

            // then
            verify(scheduledTransferRepository, times(1)).save(any(ScheduledTransfer.class));
        }

        @Nested
        @DisplayName("실패하는 경우")
        class Context_fail {

            @Test
            @DisplayName("발신 계좌가 존재하지 않으면 예외가 발생한다.")
            void fail_from_account_not_found() {
                given(accountRepository.findById(1L)).willReturn(Optional.empty());

                assertThatThrownBy(() -> transferSchedulerService.saveScheduledTransfer(1L, "222222", amount, "ONCE", validTime, null))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);
            }

            @Test
            @DisplayName("수신 계좌번호가 존재하지 않으면 예외가 발생한다.")
            void fail_to_account_not_found() {
                given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
                given(accountRepository.findByAccountNumber("222222")).willReturn(Optional.empty());

                assertThatThrownBy(() -> transferSchedulerService.saveScheduledTransfer(1L, "222222", amount, "ONCE", validTime, null))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_NOT_FOUND);
            }

            @Test
            @DisplayName("발신 계좌가 삭제된 상태면 예외가 발생한다.")
            void fail_from_account_deleted() {
                Account deletedAccount = Account.builder().id(1L).deleted(true).build();
                given(accountRepository.findById(1L)).willReturn(Optional.of(deletedAccount));
                given(accountRepository.findByAccountNumber("222222")).willReturn(Optional.of(toAccount));

                assertThatThrownBy(() -> transferSchedulerService.saveScheduledTransfer(1L, "222222", amount, "ONCE", validTime, null))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_DELETED);
            }

            @Test
            @DisplayName("수신 계좌가 삭제된 상태면 예외가 발생한다.")
            void fail_to_account_deleted() {
                Account deletedToAccount = Account.builder().id(2L).accountNumber("222222").deleted(true).build();
                given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
                given(accountRepository.findByAccountNumber("222222")).willReturn(Optional.of(deletedToAccount));

                assertThatThrownBy(() -> transferSchedulerService.saveScheduledTransfer(1L, "222222", amount, "ONCE", validTime, null))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCOUNT_ALREADY_DELETED);
            }

            @Test
            @DisplayName("일회성(ONCE) 예약 시 시간이 10분 단위가 아니면 예외가 발생한다.")
            void fail_invalid_time_unit() {
                // given: 15분 (10분 단위가 아님)
                LocalDateTime invalidTimeUnit = LocalDateTime.now().plusDays(1).withMinute(15);
                given(accountRepository.findById(1L)).willReturn(Optional.of(fromAccount));
                given(accountRepository.findByAccountNumber("222222")).willReturn(Optional.of(toAccount));

                // when & then
                assertThatThrownBy(() -> transferSchedulerService.saveScheduledTransfer(1L, "222222", amount, "ONCE", invalidTimeUnit, null))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TRANSFER_TIME_UNIT);
            }
        }
    }
}