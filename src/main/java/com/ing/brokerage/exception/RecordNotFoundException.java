package com.ing.brokerage.exception;

public class RecordNotFoundException extends BaseException {

    public RecordNotFoundException(String msg) {
        super(msg);
    }

    public RecordNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
