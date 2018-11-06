package com.silaev.analytics.controller;

import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;
import com.silaev.analytics.exception.StaleTransactionException;
import com.silaev.analytics.exception.TransactionInTheFutureException;
import com.silaev.analytics.service.TransactionService;
import com.silaev.analytics.util.DateTimeConverter;
import com.silaev.analytics.util.TransactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
public class TransactionController {

    private static int MAX_OFFSET;
    private final TransactionService transactionService;
    private final DateTimeConverter dateTimeConverter;

    /**
     * This endpoint is called to create a new transaction.
     * It MUST execute in constant time and memory (O(1)).
     * @param dto coming from a request
     * @return
     */
    @PostMapping(value = "/transactions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransactionDto> postTransaction(
            @RequestBody @Validated TransactionDto dto) {

        log.debug("postTransaction :{}", dto);

        Instant instantNow = dateTimeConverter.getInstantNow();
        Instant instantDto = dto.getZonedDateTime().toInstant();

        long offset = TransactionUtil.getOffset(instantDto, instantNow);
        if (offset > MAX_OFFSET) {
            throw new StaleTransactionException("The transaction is older than 60 seconds");
        } else if (offset < 0) {
            throw new TransactionInTheFutureException("Transaction's date is in the future");
        }

        // Builder suits here more than
        // TransactionDtoToTransactionConverter
        BigDecimal amount = TransactionUtil.setScaleAndRound(dto.getAmount());
        Transaction transaction = Transaction.builder()
                .offset(offset)
                .timestamp(instantDto)
                .amount(amount)
                .count(1L)
                .max(amount)
                .min(amount)
                .build();

        TransactionDto transactionDto =
                transactionService.updateTransactionStatistics(transaction, instantNow);

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionDto);
    }

    @DeleteMapping(value = "/transactions")
    public ResponseEntity deleteTransactionStatistics() {
        log.debug("deleteTransactionStatistics");
        transactionService.cleanTransactionStatistics();
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/statistics",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SummaryStatisticDto> getTransactionStatistics() {
        log.debug("getTransactionStatistics");

        Instant instantNow = dateTimeConverter.getInstantNow();
        SummaryStatisticDto transactionStatistics = transactionService.getTransactionStatistics(instantNow);
        return ResponseEntity.ok(transactionStatistics);
    }

    public int getMaxOffset() {
        return MAX_OFFSET;
    }

    @Value("${max-offset}")
    public void setMaxOffset(int lastMilli) {
        MAX_OFFSET = lastMilli;
    }
}
