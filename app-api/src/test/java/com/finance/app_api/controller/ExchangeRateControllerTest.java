package com.finance.app_api.controller;

import com.finance.domain.external.RedisClient;
import com.finance.domain.external.RestTemplateClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestClient;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ExchangeRateController 통합 테스트")
class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestTemplateClient restTemplateClient;

    @MockitoBean
    private RedisClient redisClient;

    @Nested
    @DisplayName("GET /api/v1/exchange/rate 조시는")
    class Describe_getRate {

        @Test
        @DisplayName("성공: Redis 캐시에 값이 있으면 캐시된 환율을 반환한다.")
        void success_with_redis_cache() throws Exception {
            // given
            String from = "KRW";
            String to = "USD";
            String cacheKey = "exchange_rate:KRW:USD";
            given(redisClient.get(cacheKey)).willReturn("1350.5");

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/v1/exchange/rate")
                    .param("from", from)
                    .param("to", to)
                    .contentType(MediaType.APPLICATION_JSON));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.exchangeRate").value(1350.5))
                    .andExpect(jsonPath("$.data.fromCurrency").value("KRW"))
                    .andExpect(jsonPath("$.data.toCurrency").value("USD"))
                    .andDo(print());

            // 캐시가 적중했으므로 외부 API는 호출되지 않아야 함
            verify(restTemplateClient, never()).fetchRateFromApi(anyString(), anyString());
        }

        @Test
        @DisplayName("성공: Redis에 값이 없으면 외부 API를 호출하여 결과를 반환하고 캐시에 저장한다.")
        void success_with_external_api() throws Exception {
            // given
            String from = "USD";
            String to = "KRW";
            String cacheKey = "exchange_rate:USD:KRW";

            given(redisClient.get(cacheKey)).willReturn(null); // 캐시 미스
            given(restTemplateClient.fetchRateFromApi("USD", "KRW")).willReturn(1400.0);

            // when
            ResultActions resultActions = mockMvc.perform(get("/api/v1/exchange/rate")
                    .param("from", from)
                    .param("to", to));

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.exchangeRate").value(1400.0))
                    .andDo(print());

            // 외부 API 호출 확인 및 Redis 저장 확인
            verify(restTemplateClient, times(1)).fetchRateFromApi("USD", "KRW");
            verify(redisClient, times(1)).set(eq(cacheKey), eq("1400.0"), any());
        }

        @Test
        @DisplayName("성공: 파라미터가 소문자로 들어와도 대문자로 정규화하여 처리한다.")
        void success_with_lowercase_params() throws Exception {
            // given
            given(redisClient.get("exchange_rate:KRW:USD")).willReturn("1300.0");

            // when
            mockMvc.perform(get("/api/v1/exchange/rate")
                            .param("from", "krw") // 소문자
                            .param("to", "usd")) // 소문자
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fromCurrency").value("KRW"))
                    .andExpect(jsonPath("$.data.toCurrency").value("USD"));
        }
    }
}