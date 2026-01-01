package com.finance.app_api.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "금액은 필수 입력값입니다.")
    @Positive(message = "금액은 0보다 커야 합니다.")
    private Long amount; // 금액
}
