package com.finance.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE("error.invalid.input"),
    INTERNAL_SERVER_ERROR("error.internal.server"),
    EXTERNAL_API_ERROR("error.external.api.error"),

    // 계좌 오류
    ACCOUNT_NOT_FOUND("error.account.not.found"),
    ACCOUNT_ALREADY_DELETED("error.account.already.deleted"),
    ACCOUNT_NUMBER_DUPLICATED("error.account.number.duplicated"),
    ACCOUNT_HAS_BALANCE("error.account.balance.exists"),

    // 송금 오류
    INSUFFICIENT_BALANCE("error.balance.insufficient"),
    TRANSFER_TARGET_SAME("error.transfer.target.same"),

    // 일 한도 초과 오류 (출금 / 이체)
    DAILY_WITHDRAW_LIMIT_EXCEEDED("error.limit.withdraw.exceeded"),
    DAILY_TRANSFER_LIMIT_EXCEEDED("error.limit.transfer.exceeded"),

    // 자동 이체 & 예약 이체 오류
    INVALID_TRANSFER_STATUS_CHANGE("error.invalid.transfer.status.change"),
    TRANSFER_CANNOT_BE_RESUMED("error.transfer.cannot.be.resumed"),
    INVALID_RESERVATION_TIME("error.invalid.reservation.time"),
    INVALID_RECURRING_DAY("error.invalid.recurring.day"),
    INVALID_TRANSFER_TIME_UNIT("error.invalid.transfer.time.unit"),

    // 환율 데이터 오류
    INVALID_CURRENCY_CODE("error.invalid.currency.code");

    private final String messageKey;
}
