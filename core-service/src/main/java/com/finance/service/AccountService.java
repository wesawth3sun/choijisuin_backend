package com.finance.service;


import com.finance.domain.model.e.Currency;

public interface AccountService {
    // 계좌 등록
    Long registerAccount(String accountNumber, Currency currency);

    // 계좌 삭제
    void deleteAccount(Long id);
}
