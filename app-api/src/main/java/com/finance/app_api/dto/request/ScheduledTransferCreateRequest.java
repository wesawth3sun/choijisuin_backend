package com.finance.app_api.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
public class ScheduledTransferCreateRequest {

    @NotNull(message = "출금 계좌 ID는 필수입니다.")
    private Long fromAccountId;

    @NotBlank(message = "수취 계좌 번호는 필수입니다.")
    private String toAccountNumber;

    @NotNull(message = "이체 금액은 필수입니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    private Long amount;

    @NotNull(message = "이체 타입(예약/자동)은 필수입니다.")
    private String type;

    private LocalDateTime executionTime; // 예약 이체시 사용 (서비스 로직에서 추가 검증)

    @Min(value = 1, message = "자동 이체일은 1일 이상이어야 합니다.")
    @Max(value = 31, message = "자동 이체일은 31일 이하이어야 합니다.")
    private Integer recurringDay; // 값이 비어있어도 되지만, 있다면 1~31 사이여야 함
}
