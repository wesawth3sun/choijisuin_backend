package com.finance.app_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionWithdrawRequest {

    @Schema(description = "출금 금액", example = "100000")
    @NotNull(message = "금액은 필수 입력값입니다.")
    @Positive(message = "금액은 0보다 커야 합니다.")
    private Long amount; // 금액
}
