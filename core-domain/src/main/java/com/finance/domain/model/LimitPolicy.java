package com.finance.domain.model;

public interface LimitPolicy {
    // 출금 한도 조회
    Money getDailyWithdrawLimit();

    // 이체 한도 조회
    Money getDailyTransferLimit();
}