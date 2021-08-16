package com.pccw.ruby.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    UNKNOWN_ERROR("000000"),
    VALIDATION_ERROR("000001"),
    ILLEGAL_ARGUMENT("000002"),
    DATA_NOT_FOUND("000003"),
    DATA_DUPLICATION("000004"),
    FEIGN_ERROR("000005");

    private final String code;
}
