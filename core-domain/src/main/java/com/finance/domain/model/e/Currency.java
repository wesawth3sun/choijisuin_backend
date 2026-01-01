package com.finance.domain.model.e;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Currency {
    KRW("원"),
    USD("달러");

    private final String description;

    public static Currency toCurrency(String currencyCode) {
        if (currencyCode.equals("USD")) {
            return Currency.USD;
        } else {
            return Currency.KRW;
        }
    }
}
