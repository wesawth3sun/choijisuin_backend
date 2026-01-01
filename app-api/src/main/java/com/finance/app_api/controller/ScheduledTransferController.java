package com.finance.app_api.controller;

import com.finance.app_api.dto.request.ScheduledTransferCreateRequest;
import com.finance.app_api.rsdata.RsData;
import com.finance.service.TransferSchedulerService;
import com.finance.service.dto.ReservationTimeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scheduled-transfers")
@RequiredArgsConstructor
@Tag(name = "Scheduled Transfer", description = "예약 및 자동 이체 관리 API (예약 가능 시간 조회 및 신청)")
public class ScheduledTransferController {

    private final TransferSchedulerService scheduledTransferService;

    @GetMapping("/available-times")
    @Operation(
            summary = "예약 가능 시간대 조회",
            description = "현재 시간을 기준으로 10분 단위 올림 처리된 최단 예약 가능 시간과 최대 30일 이내의 예약 가능 범위를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ReservationTimeResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content)
    })
    public ResponseEntity<RsData<ReservationTimeResponse>> getAvailableTimes() {
        ReservationTimeResponse availableReservationTimes = scheduledTransferService.getAvailableReservationTimes(); //
        RsData<ReservationTimeResponse> response = new RsData<>(
                "200",
                "예약 가능 시간 조회 성공",
                availableReservationTimes
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(
            summary = "예약/자동 이체 등록",
            description = "단건 예약(ONCE) 또는 매달 정기 이체(MONTHLY)를 등록합니다. 예약 이체는 10분 단위로만 설정 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (계좌 미존재, 삭제된 계좌, 시간 단위 불일치 등)")
    })
    public ResponseEntity<RsData<Void>> registerScheduledTransfer(
            @RequestBody @Validated ScheduledTransferCreateRequest request) {

        scheduledTransferService.saveScheduledTransfer(
                request.getFromAccountId(),
                request.getToAccountNumber(),
                request.getAmount(),
                request.getType(),
                request.getExecutionTime(),
                request.getRecurringDay()
        ); //
        RsData<Void> response = new RsData<>(
                "201",
                "예약 등록 성공"
        );
        return ResponseEntity.ok(response);
    }
}