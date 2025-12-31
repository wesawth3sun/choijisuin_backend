package com.finance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferStatus {

    PENDING("대기 중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;
}
