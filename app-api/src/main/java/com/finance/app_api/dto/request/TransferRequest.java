package com.finance.app_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @Schema(description = "상대방 계좌 번호", example = "12345")
    @NotBlank(message = "상대방 계좌 번호는 필수입니다.")
    private String targetAccountNumber;

    @Schema(description = "이체 금액", example = "1000000")
    @NotNull(message = "이체 금액은 필수입니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    private Long amount;
}