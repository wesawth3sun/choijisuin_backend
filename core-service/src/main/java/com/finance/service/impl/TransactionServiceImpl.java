package com.finance.service.impl;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.Account;
import com.finance.domain.model.LimitPolicy;
import com.finance.domain.model.Money;
import com.finance.domain.model.Transaction;
import com.finance.domain.model.e.TransactionType;
import com.finance.domain.repository.AccountRepository;
import com.finance.domain.repository.TransactionRepository;
import com.finance.service.TransactionService;
import com.finance.service.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 한도 조회의 경우 = 일 누적합을 redis 에 캐싱해 두고, 분산락을 통해서 조회하도록 확장 가능
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LimitPolicy limitPolicy;

    /**
     * 특정 계좌에 금액을 입금하고 거래 내역을 기록합니다.
     * @param accountId 입금할 대상 계좌의 고유 식별자
     * @param amount 입금할 금액 (0원 이상의 양수여야 함)
     *
     * @throws BusinessException 계좌를 찾을 수 없는 경우 ({@link ErrorCode#ACCOUNT_NOT_FOUND})
     */
    @Transactional
    @Override
    public void deposit(Long accountId, Long amount) {
        log.info("[TRANSACTION] 입금 요청: accountId={}, amount={}", accountId, amount);

        // 1. 계좌 조회
        Account account = getAccount(accountId);
        if (account.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DELETED); // 이미 삭제된 계좌는 출금 불가
        }

        // 2. 계좌 잔액 갱신
        account.deposit(Money.of(amount));

        // 3. 거래 내역 생성 및 저장
        Transaction deposit = Transaction.createDeposit(account, Money.of(amount));

        // 4. account, transaction 저장
        accountRepository.save(account);
        transactionRepository.save(deposit);

        log.info("[TRANSACTION] 입금 완료: account 잔액={}, 거래 내역={}", account.getBalance(), deposit);
    }

    /**
     * 특정 계좌에서 금액을 출금하고 거래 내역을 기록합니다.
     * @param accountId 출금할 대상 계좌의 고유 식별자
     * @param amount 출금할 금액
     *
     * @throws BusinessException 계좌를 찾을 수 없는 경우 (ACCOUNT_NOT_FOUND)
     * @throws BusinessException 잔액이 부족한 경우 (INSUFFICIENT_BALANCE)
     * @throws BusinessException 일일 출금 한도를 초과한 경우 (DAILY_WITHDRAW_LIMIT_EXCEEDED)
     */
    @Override
    @Transactional
    public void withdraw(Long accountId, Long amount) {
        log.info("[TRANSACTION] 출금 요청: accountId={}, amount={}", accountId, amount);

        // 1. 계좌 조회
        Account account = getAccount(accountId);
        if (account.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DELETED); // 이미 삭제된 계좌는 출금 불가
        }

        // 2. 일일 누적 출금액 조회
        // 오늘 해당 계좌에서 발생한 WITHDRAW 타입 거래의 합계
        LocalDateTime today = LocalDate.now().atStartOfDay();
        Money dailyUsage = transactionRepository.findDailySumByType(
                accountId,
                today,
                TransactionType.WITHDRAW
        );
        log.info("[TRANSACTION] 일일 출금 누적합 조회 기준 시각: {}, 결과: {}", today, dailyUsage);

        // 3. 계좌 도메인 실행 (잔액 및 한도 체크 포함)
        account.withdraw(Money.of(amount), dailyUsage, limitPolicy);

        // 4. 거래 내역 생성
        Transaction withdrawTransaction = Transaction.createWithdraw(account, Money.of(amount));

        // 5. 변경된 상태 저장
        accountRepository.save(account);
        transactionRepository.save(withdrawTransaction);

        log.info("[TRANSACTION] 출금 완료: account 잔액={}, 거래 내역={}", account.getBalance(), withdrawTransaction);
    }

    /**
     * 특정 계좌에서 다른 계좌로 금액을 송금(이체)하고 거래 내역을 기록합니다.
     * <ul>
     * <li>발신 및 수신 계좌의 유효성 및 삭제 여부 확인</li>
     * <li>오늘 하루 동안의 총 이체 누적액을 조회하여 일일 한도 검증</li>
     * <li>발신 계좌에서 금액 및 수수료 차감, 수신 계좌에 금액 증액 (도메인 로직)</li>
     * <li>발신자용 '이체(출금)' 내역과 수신자용 '이체(입금)' 내역을 각각 생성 및 저장</li>
     * </ul>
     * </p>
     *
     * @param fromAccountId 송금을 보내는 발신 계좌의 고유 식별자
     * @param targetAccountNumber 송금을 받는 수신 계좌의 계좌 번호
     * @param amount 송금할 금액
     *
     * @throws BusinessException 계좌를 찾을 수 없는 경우 (ACCOUNT_NOT_FOUND)
     * @throws BusinessException 계좌가 이미 삭제된 상태인 경우 (ACCOUNT_ALREADY_DELETED)
     * @throws BusinessException 잔액이 부족한 경우 (INSUFFICIENT_BALANCE)
     * @throws BusinessException 일일 이체 한도를 초과한 경우 (DAILY_TRANSFER_LIMIT_EXCEEDED)
     */
    @Override
    @Transactional
    public void transfer(Long fromAccountId, String targetAccountNumber, Long amount) {
        log.info("[TRANSACTION] 이체 요청: fromAccountId={}, toAccountNumber={}, amount={}", fromAccountId, targetAccountNumber, amount);

        // 1. 보내는 사람, 받는 사람의 계좌 조회 (비관적 락)
        Account sender = getAccount(fromAccountId);// from 계좌 조회
        Account receiver = accountRepository.findByAccountNumberWithLock(targetAccountNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND)); // to 계좌 조회

        if (sender.isDeleted() || receiver.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        } // 이미 삭제된 계좌는 송금 발신 + 수신 불가

        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            throw new BusinessException(ErrorCode.TRANSFER_TARGET_SAME);
        }

        // 2. 일일 이체 누적액 조회 (한도 조회)
        LocalDateTime today = LocalDate.now().atStartOfDay();
        Money dailyUsage = transactionRepository.findDailySumByType(
                fromAccountId, today, TransactionType.TRANSFER_SEND);
        log.info("[TRANSACTION] 일일 이체 누적합 조회 기준 시각: {}, 결과: {}", today, dailyUsage);

        // 3. 계좌 도메인 실행 (잔액 차감, 수수료 계산, 한도 체크 포함)
        sender.transfer(receiver, Money.of(amount), dailyUsage, limitPolicy);

        // 4. 거래 내역 생성
        Money money = Money.of(amount);
        Money fee = money.calculateFee(1.0);

        Transaction transferSend = Transaction.createTransferSend(sender, receiver.getAccountNumber(), money, fee);
        Transaction transferReceive = Transaction.createTransferReceive(receiver, sender.getAccountNumber(), money);

        // 5. 저장
        accountRepository.save(sender);
        accountRepository.save(receiver);
        transactionRepository.save(transferSend);
        transactionRepository.save(transferReceive);

        log.info("[TRANSACTION] 이체 완료: fromAccount 잔액={}, toAccount 잔액={}, 거래 내역={}", sender.getBalance(), receiver.getBalance(), transferSend);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getHistory(Long accountId, int page) {
        log.info("[TRANSACTION] 거래 내역 조회: accountId={}, page={}", accountId, page);

        int pageIndex = (page < 1) ? 0 : page - 1; // 사용자가 잘못된 값을 보낼 것을 방지

        // 1. 페이지 요청 객체 생성
        // Pageable pageable = PageRequest.of(pageIndex, 20); // 페이지 번호는 0부터 시작하므로 -1 해줌

        // 2. 조회
        List<Transaction> history = transactionRepository.getHistory(accountId, pageIndex, 20);

        log.info("[TRANSACTION] 거래 내역 조회 완료: accountId={}, page={}, 거래 내역 총 {} 건",
                accountId, page, history.size());

        // 3. 엔티티를 응답 DTO로 변환
        return history.stream()
                .map(TransactionResponse::of)
                .toList();
    }

    private Account getAccount(Long accountId) {
        // 비관적 락을 사용하여 계좌 조회
        return accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    }
}
