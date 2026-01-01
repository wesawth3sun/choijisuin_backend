package com.finance.domain.model.e;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferType {
    ONCE("예약"),
    MONTHLY("자동");

    private final String description;

    public static TransferType toTransferType(String transferTypeCode) {
        if (transferTypeCode.equals("MONTHLY")) {
            return TransferType.MONTHLY;
        } else {
            return TransferType.ONCE;
        }
    }
}
