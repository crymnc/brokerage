package com.ing.brokerage.exception;

public class BusinessException extends BaseException {


    public BusinessException(String messageKey, Object... args) {

        super(messageKey, args);
    }

    public BusinessException(String msg) {

        super(msg);
    }
}
