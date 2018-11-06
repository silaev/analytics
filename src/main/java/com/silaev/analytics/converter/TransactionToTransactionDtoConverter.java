package com.silaev.analytics.converter;

import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class TransactionToTransactionDtoConverter implements Converter<Transaction, TransactionDto> {
    @Override
    public TransactionDto convert(Transaction transaction) {
        return TransactionDto.builder()
                .amount(transaction.getAmount())
                .zonedDateTime(transaction.getTimestamp().atZone(ZoneId.of("UTC")))
                .build();
    }
}
