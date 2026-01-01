package com.finance.app_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExchangeRateResponse {

    private String fromCurrency;
    private String toCurrency;
    private Double exchangeRate;
    private LocalDateTime cachedAt; // 데이터 신뢰성
}
