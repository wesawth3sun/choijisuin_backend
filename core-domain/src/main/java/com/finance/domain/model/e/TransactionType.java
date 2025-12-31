package com.finance.domain.model.e;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionType {
    DEPOSIT("입금"),
    WITHDRAW("출금"),
    TRANSFER_SEND("이체(출금)"),
    TRANSFER_RECEIVE("이체(입금)");

    private final String description;
}