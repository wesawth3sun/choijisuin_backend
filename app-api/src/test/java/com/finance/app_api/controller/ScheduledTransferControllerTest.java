package com.finance.app_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.app_api.dto.request.ScheduledTransferCreateRequest;
import com.finance.service.TransferSchedulerService;
import com.finance.service.dto.ReservationTimeResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("ScheduledTransferController 통합 테스트")
class ScheduledTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferSchedulerService scheduledTransferService;

    @Nested
    @DisplayName("GET /api/v1/scheduled-transfers/available-times")
    class Describe_getAvailableTimes {

        @Test
        @DisplayName("성공: 예약 가능한 시간 범위와 안내 메시지를 반환한다.")
        void it_returns_available_reservation_times() throws Exception {
            // given
            LocalDateTime minTime = LocalDateTime.now().plusMinutes(10);
            LocalDateTime maxTime = LocalDateTime.now().plusDays(30);
            ReservationTimeResponse mockResponse = new ReservationTimeResponse(
                    minTime, maxTime, "10분 단위로 예약이 가능합니다."
            );
            given(scheduledTransferService.getAvailableReservationTimes()).willReturn(mockResponse);

            // when & then
            mockMvc.perform(get("/api/v1/scheduled-transfers/available-times"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.notice").value("10분 단위로 예약이 가능합니다."))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/scheduled-transfers")
    class Describe_registerScheduledTransfer {

        @Test
        @DisplayName("성공: 유효한 예약 이체 요청 시 201 응답을 반환한다.")
        void it_returns_201_when_request_is_valid() throws Exception {
            // given
            ScheduledTransferCreateRequest request = new ScheduledTransferCreateRequest(
                    1L,
                    "222222",
                    10000L,
                    "ONCE",
                    LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0),
                    null
            );

            // when & then
            mockMvc.perform(post("/api/v1/scheduled-transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("201"))
                    .andExpect(jsonPath("$.msg").value("예약 등록 성공"))
                    .andDo(print());

            // 서비스 호출 여부 확인
            verify(scheduledTransferService).saveScheduledTransfer(
                    eq(1L), eq("222222"), eq(10000L), anyString(), any(), any());
        }

        @Test
        @DisplayName("실패: 필수 파라미터가 누락되면 400 에러(Validation Error)를 반환한다.")
        void it_returns_400_when_params_are_missing() throws Exception {
            // given: 빈 객체 전송
            ScheduledTransferCreateRequest invalidRequest = new ScheduledTransferCreateRequest(
                    null, null, null, null, null, null
            );

            // when & then
            mockMvc.perform(post("/api/v1/scheduled-transfers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}