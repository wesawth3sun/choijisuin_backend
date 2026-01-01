package com.finance.app_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountRequest {

    @Schema(description = "계좌 번호", example = "1234")
    @NotBlank(message = "계좌 번호는 필수 입력값입니다.")
    @Pattern(regexp = "^[0-9]*$", message = "계좌 번호는 숫자만 입력 가능합니다.")
    private String accountNumber;

    @Schema(description = "화폐 단위", example = "KRW")
    @NotNull(message = "화폐는 필수 입력값입니다.")
    private String currency;
}
