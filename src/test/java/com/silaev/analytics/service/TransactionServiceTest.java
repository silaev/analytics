package com.silaev.analytics.service;

import com.silaev.analytics.converter.TransactionSummaryStatisticsConverter;
import com.silaev.analytics.converter.TransactionToTransactionDtoConverter;
import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;
import com.silaev.analytics.util.TransactionUtil;
import com.silaev.analytics.util.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class TransactionServiceTest {
    private static final int MAX_OFFSET = 60000;

    @Spy
    private TransactionSummaryStatisticsConverter summaryStatisticsConverter;

    @Mock
    private TransactionToTransactionDtoConverter transactionDtoConverter;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        transactionService.setMaxOffset(MAX_OFFSET);

        transactionService.initStore();
    }


    @Test
    void shouldInitStore() {
        //GIVEN

        //WHEN
        transactionService.initStore();

        //THEN
        assertEquals(MAX_OFFSET + 1, transactionService.getStoreSize());
    }

    /**
     * Tests not only singe post, but also
     * 2 simultaneous posts with the same timestamp
     */
    @ParameterizedTest(name = "{index}: plusSeconds({0})")
    @ValueSource(ints = {1,0})
    void shouldUpdateTransactionStatistics(int plusSeconds) {
        //GIVEN
        double amount1 = 25.36;
        String zonedDateTimeString = "2018-10-22T11:51:20.876Z";
        TransactionDto transactionExpectedDto1 =
                TestUtils.mockTransactionDto(amount1, zonedDateTimeString);
        BigDecimal amount1Bd = transactionExpectedDto1.getAmount();
        double amount2 = 12.35;
        TransactionDto transactionDto2 =
                TestUtils.mockTransactionDto(amount2, zonedDateTimeString);
        BigDecimal amount2Bd = transactionDto2.getAmount();
        ZonedDateTime zonedDateTime = transactionExpectedDto1.getZonedDateTime();
        Instant instantNow = zonedDateTime.plusSeconds(plusSeconds).toInstant();

        long offset = 1000;//1 sec
        long count = 2L;
        BigDecimal minBd = TransactionUtil.setScaleAndRound(new BigDecimal(amount2));
        BigDecimal maxBd = TransactionUtil.setScaleAndRound(new BigDecimal(amount1));
        Transaction transaction1 = TestUtils.mockTransaction(offset,
                zonedDateTime.toInstant(),
                amount1);

        Transaction transaction2 = TestUtils.mockTransaction(offset,
                zonedDateTime.toInstant(),
                amount2);

        BigDecimal add = TransactionUtil.setScaleAndRound(amount1Bd.add(amount2Bd));
        Transaction transactionUnionExpected = Transaction.builder()
                .offset(offset)
                .timestamp(zonedDateTime.toInstant())
                .amount(add)
                .min(minBd)
                .max(maxBd)
                .count(count)
                .build();

        TransactionDto transactionUnionDtoExpexted = TransactionDto.builder()
                .zonedDateTime(zonedDateTime)
                .amount(add)
                .count(count)
                .max(minBd)
                .max(maxBd)
                .offset(offset)
                .build();
        when(transactionDtoConverter.convert(transaction1))
                .thenReturn(transactionExpectedDto1);
        when(transactionDtoConverter.convert(transactionUnionExpected))
                .thenReturn(transactionUnionDtoExpexted);

        //WHEN
        TransactionDto transactionDtoActual1 =
                transactionService.updateTransactionStatistics(transaction1, instantNow);
        TransactionDto transactionDtoActual2 =
                transactionService.updateTransactionStatistics(transaction2, instantNow);


        //THEN
        assertNotNull(transactionDtoActual1);
        assertNotNull(transactionDtoActual2);
        assertEquals(transactionExpectedDto1, transactionDtoActual1);
        assertEquals(transactionUnionDtoExpexted, transactionDtoActual2);
    }

    @Test
    void shouldGetEmptyTransactionStatistics() {
        //GIVEN
        Instant instantNow = TestUtils.INSTANT_NOW;
        SummaryStatisticDto summaryStatisticDto =
                TestUtils.mockSummaryStatisticDto("0.00", "0.00", "0.00", "0.00", 0);

        //WHEN
        SummaryStatisticDto transactionStatistics =
                transactionService.getTransactionStatistics(instantNow);

        //THEN
        assertEquals(summaryStatisticDto, transactionStatistics);
    }

    @Test
    void shouldGetTransactionStatistics() {
        //GIVEN
        double amount1 = 25.36;
        String zonedDateTimeString = "2018-10-22T11:51:20.876Z";
        TransactionDto transactionDto1 =
                TestUtils.mockTransactionDto(amount1, zonedDateTimeString);
        double amount2 = 12.35;
        TransactionDto transactionDto2 =
                TestUtils.mockTransactionDto(amount2, zonedDateTimeString);
        Instant instantNow = transactionDto1.getZonedDateTime().plusSeconds(1).toInstant();
        int offset = 1000;//1 sec
        Transaction transactionExpected1 =
                TestUtils.mockTransaction(offset, transactionDto1.getZonedDateTime().toInstant(), amount1);
        Transaction transactionExpected2 =
                TestUtils.mockTransaction(offset, transactionDto2.getZonedDateTime().toInstant(), amount2);
        transactionService.updateTransactionStatistics(transactionExpected1, instantNow);
        transactionService.updateTransactionStatistics(transactionExpected2, instantNow);
        SummaryStatisticDto summaryStatisticDto =
                TestUtils.mockSummaryStatisticDto("37.71", "18.86", "25.36", "12.35", 2);

        //WHEN
        SummaryStatisticDto transactionStatistics =
                transactionService.getTransactionStatistics(instantNow);

        //THEN
        assertEquals(summaryStatisticDto, transactionStatistics);
    }

    @Test
    void shouldCleanTransactionStatistics() {
        //GIVEN
        Instant instantNow = TestUtils.INSTANT_NOW;
        SummaryStatisticDto summaryStatisticDto =
                TestUtils.mockSummaryStatisticDto("0.00", "0.00", "0.00", "0.00", 0);

        //WHEN
        transactionService.cleanTransactionStatistics();

        //THEN
        SummaryStatisticDto transactionStatistics =
                transactionService.getTransactionStatistics(instantNow);
        assertEquals(summaryStatisticDto, transactionStatistics);
    }

    @Test
    void setMaxOffset() {
        //GIVEN
        int lastMilli = MAX_OFFSET * 2;

        //WHEN
        transactionService.setMaxOffset(lastMilli);

        //THEN
        assertEquals(lastMilli, transactionService.getMaxOffset());
    }
}