package com.finance.service.impl;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import com.finance.domain.external.RedisClient;
import com.finance.domain.external.RestTemplateClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExchangeRateServiceImpl 단위 테스트")
class ExchangeRateServiceImplUnitTest {
    @InjectMocks
    private ExchangeRateServiceImpl exchangeRateService;

    @Mock
    private RestTemplateClient exchangeRateClient;

    @Mock
    private RedisClient redisClient;

    private final String FROM = "USD";
    private final String TO = "KRW";
    private final String CACHE_KEY = "exchange_rate:USD:KRW";

    @Nested
    class GetExchangeRate {

        @Nested
        @DisplayName("캐시(Redis)에 환율 데이터가 존재하면")
        class Context_with_cache_hit {

            @Test
            @DisplayName("외부 API를 호출하지 않고 Redis의 값을 반환한다.")
            void it_returns_cached_value() {
                // given
                given(redisClient.get(CACHE_KEY)).willReturn("1300.5");

                // when
                Double result = exchangeRateService.getExchangeRate(FROM, TO);

                // then
                assertThat(result).isEqualTo(1300.5);
                verify(exchangeRateClient, never()).fetchRateFromApi(anyString(), anyString());
                verify(redisClient, never()).set(anyString(), anyString(), any());
            }
        }

        @Nested
        @DisplayName("캐시(Redis)에 환율 데이터가 존재하지 않으면")
        class Context_with_cache_miss {

            @Test
            @DisplayName("외부 API를 호출하여 환율을 가져오고 Redis에 1시간 동안 저장한다.")
            void it_fetches_from_api_and_caches() {
                // given
                Double apiRate = 1350.0;
                given(redisClient.get(CACHE_KEY)).willReturn(null);
                given(exchangeRateClient.fetchRateFromApi(FROM, TO)).willReturn(apiRate);

                // when
                Double result = exchangeRateService.getExchangeRate(FROM, TO);

                // then
                assertThat(result).isEqualTo(apiRate);
                verify(exchangeRateClient, times(1)).fetchRateFromApi(FROM, TO);
                verify(redisClient, times(1)).set(eq(CACHE_KEY), eq("1350.0"), any(Duration.class));
            }
        }

        @Nested
        @DisplayName("엣지 케이스 및 예외 상황")
        class Context_edge_cases {

            @Test
            @DisplayName("외부 API 호출 중 에러가 발생하면 BusinessException을 던진다.")
            void it_throws_exception_when_api_fails() {
                // given
                given(redisClient.get(CACHE_KEY)).willReturn(null);
                given(exchangeRateClient.fetchRateFromApi(FROM, TO))
                        .willThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

                // when & then
                assertThatThrownBy(() -> exchangeRateService.getExchangeRate(FROM, TO))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXTERNAL_API_ERROR);
            }

            @Test
            @DisplayName("Redis 서버 에러로 조회가 실패(예외 발생)할 경우의 전파를 확인한다.")
            void it_propagates_redis_exception() {
                // given
                given(redisClient.get(anyString())).willThrow(new RuntimeException("Redis connection failed"));

                // when & then
                assertThatThrownBy(() -> exchangeRateService.getExchangeRate(FROM, TO))
                        .isInstanceOf(RuntimeException.class)
                        .hasMessageContaining("Redis connection failed");
            }

            @Test
            @DisplayName("유효하지 않은 화폐 코드로 API 호출 시 예외를 던진다.")
            void it_throws_exception_for_invalid_currency() {
                // given
                String invalidTo = "INVALID";
                String key = "exchange_rate:USD:INVALID";
                given(redisClient.get(key)).willReturn(null);
                given(exchangeRateClient.fetchRateFromApi(FROM, invalidTo))
                        .willThrow(new BusinessException(ErrorCode.INVALID_CURRENCY_CODE));

                // when & then
                assertThatThrownBy(() -> exchangeRateService.getExchangeRate(FROM, invalidTo))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CURRENCY_CODE);
            }
        }
    }
}