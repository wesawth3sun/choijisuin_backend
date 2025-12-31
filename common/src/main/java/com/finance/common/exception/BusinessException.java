package com.finance.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessageKey()); // 예외 메세지 저장
        this.errorCode = errorCode;
    }
}

