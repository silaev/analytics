package com.silaev.analytics.exception;

public class TransactionInTheFutureException extends RuntimeException {
    public TransactionInTheFutureException(String s) {
        super(s);
    }

    public TransactionInTheFutureException(String s, Throwable cause) {
        super(s, cause);
    }
}
