package com.finance.service.policy;

import com.finance.domain.model.LimitPolicy;
import com.finance.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class DefaultLimitPolicy implements LimitPolicy {

    private static final Money WITHDRAW_LIMIT = Money.of(1_000_000); // 출금 한도
    private static final Money TRANSFER_LIMIT = Money.of(3_000_000); // 이체 한도

    @Override
    public Money getDailyWithdrawLimit() {
        return WITHDRAW_LIMIT;
    }

    @Override
    public Money getDailyTransferLimit() {
        return TRANSFER_LIMIT;
    }
}
