package com.finance.app_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@Schema(description = "예약/자동 이체 생성 요청 객체")
public class ScheduledTransferCreateRequest {

    @Schema(description = "출금 계좌 ID", example = "1")
    @NotNull(message = "출금 계좌 ID는 필수입니다.")
    private Long fromAccountId;

    @Schema(description = "수취 계좌 번호 (숫자만 입력)", example = "12345")
    @NotBlank(message = "수취 계좌 번호는 필수입니다.")
    private String toAccountNumber;

    @Schema(description = "이체 금액", example = "10000")
    @NotNull(message = "이체 금액은 필수입니다.")
    @Positive(message = "이체 금액은 0보다 커야 합니다.")
    private Long amount;

    @Schema(description = "이체 타입 (ONCE: 단건 예약, MONTHLY: 정기 자동이체)", example = "ONCE")
    @NotNull(message = "이체 타입(예약/자동)은 필수입니다.")
    private String type;

    @Schema(
            description = "예약 실행 시간 (ONCE 타입일 때 필수, 10분 단위로 설정 가능)",
            example = "2026-01-01T13:00:00", // 현재 시각 기준 약 30분 후 예시
            type = "string",
            format = "date-time"
    )
    private LocalDateTime executionTime;

    @Schema(description = "정기 이체일 (MONTHLY 타입일 때 사용, 1~31)", example = "10", nullable = true)
    @Min(value = 1, message = "자동 이체일은 1일 이상이어야 합니다.")
    @Max(value = 31, message = "자동 이체일은 31일 이하이어야 합니다.")
    private Integer recurringDay;
}