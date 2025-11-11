package com.ing.brokerage.exception;

public class NotAuthorizedException extends BaseException {

    public NotAuthorizedException(String msg) {

        super(msg);
    }

    public NotAuthorizedException(String messageKey, Object... args) {

        super(messageKey, args);
    }
}
