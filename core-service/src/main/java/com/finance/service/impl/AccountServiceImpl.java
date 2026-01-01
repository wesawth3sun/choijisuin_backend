package com.finance.service.impl;

import com.finance.service.AccountService;
import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.model.Account;
import com.finance.domain.model.e.Currency;
import com.finance.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    /**
     * 계좌 등록을 요청하면 DB에 새로운 계좌를 생성하고 ID를 반환합니다.
     * @param accountNumber 계좌 번호 (숫자)
     * @param currency 화폐 코드 (3자리)
     *
     * @return 계좌 ID (Long)
     */
    @Transactional
    @Override
    public Long registerAccount(String accountNumber, Currency currency) {
        log.info("[ACCOUNT] 계좌 등록 요청: {}", accountNumber);

        // 계좌 번호 중복 체크
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            log.warn("[ACCOUNT] 계좌 번호 중복: {}", accountNumber);
            throw new BusinessException(ErrorCode.ACCOUNT_NUMBER_DUPLICATED);
        }

        Account account = Account.create(accountNumber, currency);
        Account saved = accountRepository.save(account);

        log.info("[ACCOUNT] 계좌 등록 완료: {}", saved);
        return saved.getId(); // 계좌 ID 값
    }

    /**
     * 계좌 삭제를 요청하면 해당 계좌를 삭제합니다. (soft delete)
     * @param id 계좌 ID (Long)
     */
    @Transactional
    @Override
    public void deleteAccount(Long id) {
        log.info("[ACCOUNT] 계좌 삭제 요청: {}", id);

        Account find = accountRepository.findByIdWithLock(id).orElseThrow(
                () -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND) // 없으면 예외 발생
        );

        // 이미 삭제된 계좌인지 확인
        if (find.isDeleted()) {
            throw new BusinessException(ErrorCode.ACCOUNT_ALREADY_DELETED);
        }

        // 삭제 가능 여부 확인 + 삭제 플래그 변경
        accountRepository.save(find.delete()); // 삭제 플래그 변경 후 저장

        log.info("[ACCOUNT] 계좌 삭제 완료: {}", find);
    }
}
