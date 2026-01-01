package com.finance.external.api;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.external.RestTemplateClient;
import com.finance.external.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalClientImpl implements RestTemplateClient {

    @Value("${exchange.api.key}") // application.yml에 저장된 API 키
    private String apiKey;
    private final RestTemplate restTemplate;

    public Double fetchRateFromApi(String fromCurrency, String toCurrency) {
        String url = String.format(
                "https://v6.exchangerate-api.com/v6/%s/latest/%s",
                apiKey,
                fromCurrency
        );

        try {
            ExchangeRateResponse response = restTemplate.getForObject(url, ExchangeRateResponse.class);
            if (response != null && "success".equals(response.getResult())) {
                Double rate = response.getConversion_rates().get(toCurrency);
                if (rate == null) {
                    throw new BusinessException(ErrorCode.INVALID_CURRENCY_CODE);
                }
                return rate;
            }
        } catch (Exception e) {
            // 예외 로그 기록
            log.error("환율 API 호출 중 오류 발생: {}", e.getMessage());

            // 슬랙으로 알림 전송 등
        }

        throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
    }
}
