package com.finance.app_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.app_api.dto.request.TransactionDepositRequest;
import com.finance.app_api.dto.request.TransferRequest;
import com.finance.service.TransactionService;
import com.finance.service.dto.TransactionResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransactionController 통합 테스트")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Nested
    @DisplayName("POST /api/v1/transactions/{accountId}/deposit")
    class Describe_deposit {
        @Test
        @DisplayName("성공: 특정 계좌에 입금하면 200 응답을 반환한다.")
        void it_returns_200_when_deposit_success() throws Exception {
            // given
            Long accountId = 1L;
            TransactionDepositRequest request = new TransactionDepositRequest(10000L);

            // when & then
            mockMvc.perform(post("/api/v1/transactions/{accountId}/deposit", accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.msg").value("입금 성공"))
                    .andDo(print());

            verify(transactionService).deposit(eq(accountId), eq(10000L));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/transactions/{accountId}/transfer")
    class Describe_transfer {
        @Test
        @DisplayName("성공: 다른 계좌로 이체하면 200 응답을 반환한다.")
        void it_returns_200_when_transfer_success() throws Exception {
            // given
            Long accountId = 1L;
            TransferRequest request = new TransferRequest("222-222-2222", 5000L);

            // when & then
            mockMvc.perform(post("/api/v1/transactions/{accountId}/transfer", accountId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.msg").value("이체 성공"))
                    .andDo(print());

            verify(transactionService).transfer(eq(accountId), eq("222-222-2222"), eq(5000L));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/transactions/{accountId}")
    class Describe_getHistory {
        @Test
        @DisplayName("성공: 거래 내역을 조회하면 내역 리스트와 200 응답을 반환한다.")
        void it_returns_history_list() throws Exception {
            // given
            Long accountId = 1L;
            int page = 1;
            TransactionResponse responseDto = new TransactionResponse(
                    new BigDecimal("10000"),
                    new BigDecimal("0"),
                    "입금",
                    new BigDecimal("50000"),
                    LocalDateTime.now(),
                    "MY_ACCOUNT"
            );
            given(transactionService.getHistory(eq(accountId), anyInt())).willReturn(List.of(responseDto));

            // when & then
            mockMvc.perform(get("/api/v1/transactions/{accountId}", accountId)
                            .param("page", String.valueOf(page)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data[0].amount").value(10000))
                    .andExpect(jsonPath("$.data[0].type").value("입금"))
                    .andDo(print());
        }
    }
}