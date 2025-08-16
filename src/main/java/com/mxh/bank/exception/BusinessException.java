package com.mxh.bank.exception;

import lombok.Getter;

import java.util.Arrays;

public class BusinessException extends RuntimeException {

    @Getter
    private final String errorCode;

    private final Object[] args;

    public BusinessException(String errorCode, Object... args) {
        this.errorCode = errorCode;
        this.args = args;
    }

    public Object[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }
}
