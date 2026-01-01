package com.finance.service;


public interface AccountService {
    // 계좌 등록
    Long registerAccount(String accountNumber, String currency);

    // 계좌 삭제
    void deleteAccount(Long id);
}
