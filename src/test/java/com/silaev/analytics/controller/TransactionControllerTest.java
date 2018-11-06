package com.silaev.analytics.controller;

import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;
import com.silaev.analytics.exception.StaleTransactionException;
import com.silaev.analytics.exception.TransactionInTheFutureException;
import com.silaev.analytics.service.TransactionService;
import com.silaev.analytics.util.DateTimeConverter;
import com.silaev.analytics.util.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class TransactionControllerTest {
    private static final int MAX_OFFSET = 60000;

    @Mock
    private TransactionService transactionService;

    @Mock
    private DateTimeConverter dateTimeConverter;

    @InjectMocks
    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        transactionController.setMaxOffset(MAX_OFFSET);
    }

    @Test
    void shouldPostTransaction() {
        //GIVEN
        double amount = 25.36;
        String zonedDateTimeString = "2018-10-22T11:51:20.876Z";
        TransactionDto transactionDto =
                TestUtils.mockTransactionDto(amount, zonedDateTimeString);
        Instant instantNow =
                transactionDto.getZonedDateTime().plusSeconds(1).toInstant();
        when(dateTimeConverter.getInstantNow())
                .thenReturn(instantNow);
        int offset = 1000;//1 sec
        Transaction transactionExpected =
                TestUtils.mockTransaction(offset, transactionDto.getZonedDateTime().toInstant(), amount);
        when(transactionService.updateTransactionStatistics(transactionExpected, instantNow))
                .thenReturn(transactionDto);

        //WHEN
        ResponseEntity<TransactionDto> responseEntity =
                transactionController.postTransaction(transactionDto);
        TransactionDto dto = responseEntity.getBody();

        //THEN
        assertNotNull(dto);
        assertEquals(transactionDto, dto);
    }

    @Test
    void shouldThrowStaleTransactionExceptionWhilePostTransaction() {
        //GIVEN
        double amount = 25.36;
        String zonedDateTimeString = "2018-10-22T11:51:20.876Z";
        TransactionDto transactionDto =
                TestUtils.mockTransactionDto(amount, zonedDateTimeString);
        Instant instantNow = transactionDto.getZonedDateTime().plusSeconds(MAX_OFFSET + 1).toInstant();
        when(dateTimeConverter.getInstantNow()).thenReturn(instantNow);

        //WHEN
        Executable executable =
                () -> transactionController.postTransaction(transactionDto);

        //THEN
        assertThrows(StaleTransactionException.class, executable);
    }

    @Test
    public void shouldThrowTransactionInTheFutureExceptionWhilePostTransaction() {
        //GIVEN
        double amount = 25.36;
        String zonedDateTimeString = "2018-10-22T11:51:20.876Z";
        TransactionDto transactionDto =
                TestUtils.mockTransactionDto(amount, zonedDateTimeString);
        Instant instantNow = transactionDto.getZonedDateTime().minusSeconds(1).toInstant();
        when(dateTimeConverter.getInstantNow()).thenReturn(instantNow);

        //WHEN
        Executable executable =
                () -> transactionController.postTransaction(transactionDto);

        //THEN
        assertThrows(TransactionInTheFutureException.class, executable);
    }


    @Test
    void shouldDeleteTransactionStatistics() {
        //GIVEN

        //WHEN
        ResponseEntity responseEntity = transactionController.deleteTransactionStatistics();

        //THEN
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(transactionService, times(1)).cleanTransactionStatistics();

    }

    @Test
    void getTransactionStatistics() {
        //GIVEN
        SummaryStatisticDto summaryStatisticDto =
                TestUtils.mockSummaryStatisticDto("14.72", "7.36", "9.36", "5.36", 2);
        when(dateTimeConverter.getInstantNow())
                .thenReturn(TestUtils.INSTANT_NOW);
        when(transactionService.getTransactionStatistics(TestUtils.INSTANT_NOW))
                .thenReturn(summaryStatisticDto);

        //WHEN
        ResponseEntity<SummaryStatisticDto> transactionStatistics =
                transactionController.getTransactionStatistics();
        SummaryStatisticDto body = transactionStatistics.getBody();

        //THEN
        assertNotNull(body);
        assertEquals(HttpStatus.OK, transactionStatistics.getStatusCode());
        assertEquals(summaryStatisticDto, body);
    }

    @Test
    void shouldSetMaxOffset() {
        //GIVEN
        int lastMilli = MAX_OFFSET * 2;

        //WHEN
        transactionController.setMaxOffset(lastMilli);

        //THEN
        assertEquals(lastMilli, transactionController.getMaxOffset());
    }
}