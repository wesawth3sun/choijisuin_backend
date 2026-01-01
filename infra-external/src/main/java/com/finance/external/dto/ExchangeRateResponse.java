package com.finance.external.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ExchangeRateResponse {

    private String result;
    private String base_code;
    private Map<String, Double> conversion_rates; // 각 통화별 환율 정보
    private String time_last_update_utc; // 환율 업데이트 일시
}
