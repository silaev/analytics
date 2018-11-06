package com.silaev.analytics.converter;

import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.entity.Transaction;
import com.silaev.analytics.util.TransactionSummaryStatistics;
import com.silaev.analytics.util.TransactionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionSummaryStatisticsConverterTest {

    private TransactionSummaryStatisticsConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TransactionSummaryStatisticsConverter();
    }

    @Test
    void shouldConvertToDto() {
        //GIVEN
        TransactionSummaryStatistics bds =
                new TransactionSummaryStatistics();
        BigDecimal amount1 = TransactionUtil.setScaleAndRound(new BigDecimal(12.35));
        BigDecimal amount2 = TransactionUtil.setScaleAndRound(new BigDecimal(25.36));
        long count = 1L;
        Transaction transaction1 = Transaction.builder()
                .amount(amount1)
                .min(amount1)
                .max(amount1)
                .count(count)
                .build();
        Transaction transaction2 = Transaction.builder()
                .amount(amount2)
                .min(amount2)
                .max(amount2)
                .count(count)
                .build();

        bds.accept(transaction1);
        bds.accept(transaction2);

        //WHEN
        SummaryStatisticDto convert = converter.convert(bds);

        //THEN
        assertNotNull(convert);
        assertEquals("37.71", convert.getSum());
        assertEquals("18.86", convert.getAvg());
        assertEquals("25.36", convert.getMax());
        assertEquals("12.35", convert.getMin());
        assertEquals(2, (long) convert.getCount());
    }

    @Test
    void shouldConvertToNull() {
        //GIVEN
        TransactionSummaryStatistics bds = null;

        //WHEN
        SummaryStatisticDto convert = converter.convert(bds);

        //THEN
        assertNull(convert);
    }
}