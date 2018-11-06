package com.silaev.analytics.converter;


import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.util.TransactionSummaryStatistics;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.math.MathContext;

/**
 * Converts TransactionSummaryStatistics to SummaryStatisticDto.
 */
@Component
public class TransactionSummaryStatisticsConverter implements Converter<TransactionSummaryStatistics, SummaryStatisticDto> {

    @Override
    public SummaryStatisticDto convert(TransactionSummaryStatistics bds) {
        if (bds == null) {
            return null;
        }

        return SummaryStatisticDto.builder()
                .max(bds.getMax().toString())
                .min(bds.getMin().toString())
                .avg(bds.getAverage(MathContext.DECIMAL128).toString())
                .sum(bds.getSum().toString())
                .count(bds.getCount())
                .build();
    }
}
