package com.finance.app_api.controller;

import com.finance.app_api.dto.request.AccountRequest;
import com.finance.app_api.rsdata.RsData;
import com.finance.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "계좌 관련 API")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "신규 계좌 등록",
            description = "사용자의 새로운 계좌 번호를 시스템에 등록합니다. 초기 잔액은 0원으로 설정됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "계좌 등록 성공",
                    useReturnTypeSchema = true
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "중복된 계좌 번호"
            )
    })
    @PostMapping
    public ResponseEntity<RsData<Long>> registerAccount(
            @Validated @RequestBody
            @Parameter(description = "계좌 등록 정보 (계좌번호, 국가 필수)", required = true) AccountRequest request) {

        Long id = accountService.registerAccount(
                request.getAccountNumber(),
                request.getCurrency()
        );

        RsData<Long> response = new RsData<>(
                "201",
                "계좌 등록 성공",
                id
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "계좌 삭제 (Soft Delete)",
            description = "계좌 ID를 통해 계좌를 삭제 처리합니다. 실제 데이터를 삭제하지 않고 삭제 플래그(deleted)를 true로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "계좌 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 이미 삭제된 계좌"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<RsData<Void>> deleteAccount(
            @PathVariable
            @Parameter(description = "삭제할 계좌의 고유 식별자(ID)", example = "1") Long id) {

        accountService.deleteAccount(id);

        RsData<Void> response = new RsData<>(
                "200",
                "계좌 삭제 성공"
        );

        return ResponseEntity.ok(response);
    }
}
