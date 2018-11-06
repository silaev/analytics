package com.silaev.analytics.exception;

public class StaleTransactionException extends RuntimeException {
    public StaleTransactionException(String s) {
        super(s);
    }

    public StaleTransactionException(String s, Throwable cause) {
        super(s, cause);
    }
}
