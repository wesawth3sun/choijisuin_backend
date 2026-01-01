package com.finance.service.impl;

import com.finance.domain.external.RedisClient;
import com.finance.domain.external.RestTemplateClient;
import com.finance.service.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * webclinet 혹은 openfeign 을 활용해서 요청을 보내는 형식으로 코드 확장 가능
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateServiceImpl implements ExchangeRateService {

    /**
     * 외부 API 및 Redis 격리 (DIP 적용)
     */
    private final RestTemplateClient exchangeRateClient;
    private final RedisClient redisClient;

    private static final String REDIS_KEY_PREFIX = "exchange_rate:";

    @Override
    public Double getExchangeRate(String fromCurrency, String toCurrency) {
        String cacheKey = REDIS_KEY_PREFIX + fromCurrency + ":" + toCurrency;

        // 1. Redis 에서 데이터 조회
        String cachedRate = redisClient.get(cacheKey);
        if (cachedRate != null) {
            log.info("[CACHE HIT] {} -> {} 환율을 Redis에서 가져옵니다.", fromCurrency, toCurrency);
            return Double.parseDouble(cachedRate);
        }

        // 2. Redis에 값이 없으면 외부 API 호출
        log.info("[CACHE MISS] 외부 API로부터 {} -> {} 환율을 요청합니다.", fromCurrency, toCurrency);
        Double rate = exchangeRateClient.fetchRateFromApi(fromCurrency, toCurrency);

        // 3. Redis에 저장 (1시간 후 자동 만료 - 지속적 갱신)
        redisClient.set(cacheKey, rate.toString(), Duration.ofHours(1));

        return rate;
    }
}
