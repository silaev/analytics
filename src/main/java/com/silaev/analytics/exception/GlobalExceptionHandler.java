package com.silaev.analytics.exception;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.xml.bind.ValidationException;

/**
 * Handles exception withing the whole application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({InvalidFormatException.class, ValidationException.class, TransactionInTheFutureException.class})
    public ResponseEntity<String> unProcessableEntityException(RuntimeException rte) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(rte.getMessage());
    }

    @ExceptionHandler({JsonParseException.class})
    public ResponseEntity<String> invalidJsonEntityException(RuntimeException rte) {
        return ResponseEntity.badRequest().body(rte.getMessage());
    }

    @ExceptionHandler(StaleTransactionException.class)
    public ResponseEntity<String> staleTransactionException(RuntimeException rte) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(rte.getMessage());
    }
}
