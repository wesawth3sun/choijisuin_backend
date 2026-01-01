package com.finance.app_api.controller;

import com.finance.app_api.dto.request.TransactionDepositRequest;
import com.finance.app_api.dto.request.TransactionWithdrawRequest;
import com.finance.app_api.dto.request.TransferRequest;
import com.finance.app_api.rsdata.RsData;
import com.finance.service.TransactionService;
import com.finance.service.dto.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transaction", description = "계좌 거래 관리 API (입금, 출금, 이체, 내역 조회)")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * @param accountId 입금 대상 계좌의 ID (Path Variable)
     * @param request   입금 정보 (금액)
     * @return 성공 시 RsData 응답 (200)
     */
    @Operation(summary = "계좌 입금", description = "지정한 계좌 ID로 금액을 입금합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "입금 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없음"
            )
    })
    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<RsData<String>> deposit(
            @PathVariable @Parameter(description = "계좌 식별자(ID)", example = "1") Long accountId,
            @RequestBody @Validated TransactionDepositRequest request) {

        transactionService.deposit(accountId, request.getAmount());

        RsData<String> response = new RsData<>(
                "200",
                "입금 성공"
        );

        return ResponseEntity.ok(response);
    }


    /**
     * @param accountId 출금 대상 계좌의 ID (Path Variable)
     * @param request   출금 정보 (금액 등)
     * @return 성공 시 RsData 응답 (200)
     */
    @Operation(summary = "계좌 출금", description = "계좌에서 금액을 출금합니다. 일일 출금 한도(100만원)를 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "출금 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잔액 부족 또는 일 한도 초과"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없음"
            )
    })
    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<RsData<String>> withdraw(
            @PathVariable @Parameter(description = "계좌 식별자(ID)", example = "1") Long accountId,
            @RequestBody @Validated TransactionWithdrawRequest request) {

        transactionService.withdraw(accountId, request.getAmount());

        RsData<String> response = new RsData<>(
                "200",
                "출금 성공"
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "계좌 이체", description = "타인 계좌로 송금합니다. 수수료 1%가 추가 차감되며, 일일 이체 한도(300만원)를 체크합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "이체 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잔액 부족, 한도 초과, 또는 자신에게 송금 시도"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "보내는 계좌 또는 받는 계좌를 찾을 수 없음"
            )
    })
    @PostMapping("/{accountId}/transfer")
    public ResponseEntity<RsData<String>> transfer(
            @PathVariable @Parameter(description = "보내는 사람의 계좌 ID", example = "1") Long accountId,
            @RequestBody @Validated TransferRequest request) {

        transactionService.transfer(accountId, request.getTargetAccountNumber(), request.getAmount());

        RsData<String> response = new RsData<>(
                "200",
                "이체 성공");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "거래 내역 조회", description = "특정 계좌의 거래 내역을 최신순으로 20개씩 페이징하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공 (데이터가 없으면 빈 리스트 반환)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계좌를 찾을 수 없음"
            )
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<RsData<List<TransactionResponse>>> getHistory(
            @PathVariable @Parameter(description = "조회할 계좌 ID", example = "1") Long accountId,
            @RequestParam(defaultValue = "1") @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") int page) {

        List<TransactionResponse> history = transactionService.getHistory(accountId, page);

        RsData<List<TransactionResponse>> response = new RsData<>(
                "200",
                "거래 내역 조회 성공",
                history
        );

        return ResponseEntity.ok(response);
    }
}
