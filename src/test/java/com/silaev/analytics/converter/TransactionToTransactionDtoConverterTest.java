package com.silaev.analytics.converter;

import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;
import com.silaev.analytics.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionToTransactionDtoConverterTest {

    private TransactionToTransactionDtoConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TransactionToTransactionDtoConverter();
    }

    @Test
    void shouldConvertTransactionToTransactionDto() {
        //GIVEN
        Transaction transactionExpected =
                TestUtils.mockTransaction(1000, TestUtils.INSTANT_NOW, 25.36);

        //WHEN
        TransactionDto convert = converter.convert(transactionExpected);

        //THEN
        assertNotNull(convert);
        assertEquals(transactionExpected.getAmount(), convert.getAmount());
        assertEquals(transactionExpected.getTimestamp(), convert.getZonedDateTime().toInstant());
    }

    @Test
    void shouldConvertToNull() {
        //GIVEN
        Transaction transactionExpected = null;

        //WHEN
        Transaction transaction = transactionExpected;

        //THEN
        assertNull(transaction);
    }
}