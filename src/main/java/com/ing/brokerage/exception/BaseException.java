package com.ing.brokerage.exception;

import lombok.Getter;

@Getter
public class BaseException extends Exception {

    private final String messageKey;
    private final transient Object[] args;

    public BaseException(String messageKey, Object... args) {
        this.messageKey = messageKey;
        this.args = args;
    }
}
