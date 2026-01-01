package com.finance.domain.external;

public interface RestTemplateClient {
    Double fetchRateFromApi(String fromCurrency, String toCurrency);
}
