package com.silaev.analytics.util;

import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TestUtils {

    public static final Instant INSTANT_NOW = LocalDateTime
            .of(2018, 10, 22, 11, 51, 20)
            .toInstant(ZoneOffset.UTC);
    private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
    private static final DateTimeFormatter PARSER = DateTimeFormatter.ofPattern(PATTERN).withZone(ZoneOffset.UTC);

    private TestUtils() {

    }

    public static TransactionDto mockTransactionDto(double amount, String zonedDateTimeString) {
        return TransactionDto.builder()
                .amount(TransactionUtil.setScaleAndRound(new BigDecimal(amount)))
                .zonedDateTime(ZonedDateTime.parse(zonedDateTimeString, PARSER))
                .build();
    }

    public static Transaction mockTransaction(long offset,
                                              Instant timestamp,
                                              double amount) {

        return Transaction.builder()
                .offset(offset)
                .timestamp(timestamp)
                .amount(TransactionUtil.setScaleAndRound(new BigDecimal(amount)))
                .max(TransactionUtil.setScaleAndRound(new BigDecimal(amount)))
                .min(TransactionUtil.setScaleAndRound(new BigDecimal(amount)))
                .count(1L)
                .build();
    }

    public static SummaryStatisticDto mockSummaryStatisticDto(String sum,
                                                              String avg,
                                                              String max,
                                                              String min,
                                                              long count) {

        return SummaryStatisticDto.builder()
                .sum(sum)
                .avg(avg)
                .max(max)
                .min(min)
                .count(count)
                .build();
    }
}