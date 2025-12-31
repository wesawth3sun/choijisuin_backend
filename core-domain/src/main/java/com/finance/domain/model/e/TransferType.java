package com.finance.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferType {
    ONCE("예약"),
    MONTHLY("자동");

    private final String description;
}
