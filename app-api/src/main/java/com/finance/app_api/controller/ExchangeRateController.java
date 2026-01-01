package com.finance.app_api.controller;

import com.finance.app_api.dto.response.ExchangeRateResponse;
import com.finance.app_api.rsdata.RsData;
import com.finance.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/exchange")
@RequiredArgsConstructor
@Tag(name = "Exchange Rate", description = "환율 정보 조회 API")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    /**
     * 특정 통화 쌍의 환율 조회
     * USD -> KRW 만 제공한다고 가정 (이후 추가 가능)
     * GET /api/v1/exchange/rate?from=USD&to=KRW
     */
    @GetMapping("/rate")
    @Operation(summary = "실시간 환율 조회", description = "Redis 캐시를 우선 조회하며, 없을 경우 외부 API를 호출합니다.")
    public ResponseEntity<RsData<ExchangeRateResponse>> getRate(
            @Parameter(description = "기준 화폐 (예: KRW, USD)", example = "KRW")
            @RequestParam(defaultValue = "USD") String from,

            @Parameter(description = "대상 화폐 (예: KRW, USD)", example = "USD")
            @RequestParam(defaultValue = "KRW") String to) {

        // 대문자로 정규화
        String fromCurrency = from.toUpperCase();
        String toCurrency = to.toUpperCase();

        Double rate = exchangeRateService.getExchangeRate(fromCurrency, toCurrency);

        RsData<ExchangeRateResponse> response = new RsData<>(
                "200",
                "환율 조회 성공",
                new ExchangeRateResponse(
                        fromCurrency,
                        toCurrency,
                        rate,
                        LocalDateTime.now())
        );

        return ResponseEntity.ok(response);
    }
}
