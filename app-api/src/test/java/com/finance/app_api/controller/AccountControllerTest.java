package com.finance.app_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.app_api.dto.request.AccountRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 후 DB 롤백
@DisplayName("AccountController 통합 테스트")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("계좌 등록 테스트")
    class RegisterAccount {

        @Test
        @DisplayName("성공: 올바른 계좌 번호를 전달하면 201 응답과 계좌 ID를 반환한다")
        void register_success() throws Exception {
            // given
            AccountRequest request = new AccountRequest("1101234567", "KRW");
            String jsonRequest = objectMapper.writeValueAsString(request);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("201"))
                    .andExpect(jsonPath("$.msg").value("계좌 등록 성공"))
                    .andExpect(jsonPath("$.data").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 중복된 계좌 번호로 등록을 시도하면 에러 메시지를 반환한다")
        void register_fail_duplicated() throws Exception {
            // given
            String accountNumber = "999999999";
            accountRegister(accountNumber); // 미리 등록

            AccountRequest request = new AccountRequest(accountNumber, "KRW");
            String jsonRequest = objectMapper.writeValueAsString(request);

            // when
            ResultActions resultActions = mockMvc.perform(
                    post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest)
            );

            // then
            resultActions
                    .andExpect(jsonPath("$.code").value("ACCOUNT_NUMBER_DUPLICATED"))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("계좌 삭제 테스트")
    class DeleteAccount {

        @Test
        @DisplayName("성공: 존재하는 계좌 ID를 삭제 요청하면 200 응답을 반환한다")
        void delete_success() throws Exception {
            // given
            Long accountId = accountRegister("111222333");

            // when
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/v1/accounts/{id}", accountId)
            );

            // then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.msg").value("계좌 삭제 성공"))
                    .andDo(print());
        }
    }

    // 테스트용 계좌 등록 헬퍼 메서드
    private Long accountRegister(String accountNumber) throws Exception {
        AccountRequest request = new AccountRequest(accountNumber, "KRW");
        String result = mockMvc.perform(
                post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andReturn().getResponse().getContentAsString();

        // JSON 응답에서 id 파싱 (간략화된 방식)
        return objectMapper.readTree(result).get("data").asLong();
    }
}