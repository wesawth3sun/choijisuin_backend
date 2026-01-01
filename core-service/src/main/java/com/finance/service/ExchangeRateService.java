package com.finance.service;

public interface ExchangeRateService {
    Double getExchangeRate(String fromCurrency, String toCurrency);
}
