package com.finance.app_api.exception;

import com.finance.common.exception.BusinessException;
import com.finance.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        // 로깅
        log.warn("[BUSINESS_EXCEPTION] code: {}, message: {}, detail: {}",
                errorCode.name(), errorCode.getMessageKey(), e.getMessage());

        // messages.properties 파일에서 메세지 가져오기
        String message = messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                Locale.KOREA
        );

        ErrorResponse response = ErrorResponse.of(errorCode.name(), message);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Bean Validation (@Valid) 예외 발생 시 로그 기록
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        // 발생한 에러들 중 첫 번째 에러의 기본 메시지
        String errorMessage = e.getBindingResult()
                .getAllErrors()
                .getFirst()
                .getDefaultMessage();

        log.warn("[VALIDATION_EXCEPTION] field_errors: {}", e.getBindingResult().getFieldErrors());

        // 에러 응답 객체 생성
        ErrorResponse response = ErrorResponse.of("INVALID_INPUT", errorMessage);

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 시스템 내부의 예상치 못한 런타임 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllException(Exception e) {
        log.error("[SYSTEM_ERROR] unexpected error occurred: ", e);

        ErrorResponse response = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "알 수 없는 서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.internalServerError().body(response);
    }
}