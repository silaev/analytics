package com.silaev.analytics.service;

import com.silaev.analytics.converter.TransactionSummaryStatisticsConverter;
import com.silaev.analytics.converter.TransactionToTransactionDtoConverter;
import com.silaev.analytics.dto.SummaryStatisticDto;
import com.silaev.analytics.dto.TransactionDto;
import com.silaev.analytics.entity.Transaction;
import com.silaev.analytics.util.TransactionSummaryStatistics;
import com.silaev.analytics.util.TransactionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.LongStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private static final Instant INSTANT_ZERO = Instant.ofEpochMilli(0);
    private static int MAX_OFFSET;
    private final TransactionSummaryStatisticsConverter summaryStatisticsConverter;
    private final TransactionToTransactionDtoConverter transactionDtoConverter;
    private final ConcurrentMap<Long, Transaction> store =
            new ConcurrentHashMap<>(MAX_OFFSET + 1);

    @PostConstruct
    public void initStore() {
        LongStream.rangeClosed(0, MAX_OFFSET)
                .forEach(i -> store.put(i, getValue(i)));
    }

    /**
     * Initializes store value with a stub value.
     * @param i - current index
     * @return
     */
    private Transaction getValue(long i) {

        return Transaction.builder()
                .offset(i)
                .amount(BigDecimal.ZERO)
                .timestamp(INSTANT_ZERO)
                .min(BigDecimal.ZERO)
                .max(BigDecimal.ZERO)
                .count(0L)
                .build();
    }

    /**
     * Updates transaction statistics
     *
     * @param transaction
     * @param
     */
    public TransactionDto updateTransactionStatistics(final Transaction transaction,
                                                      final Instant instantNow) {
        Long offset = transaction.getOffset();
        Transaction merge = store.merge(offset,
                transaction,
                (oldValue, newValue) -> {
                    if (TransactionUtil.isOffsetAfter(oldValue.getTimestamp(), instantNow, MAX_OFFSET)) {
                        return newValue;//old removal
                    } else {
                        //merging, only 2 are allowed at a time
                        return Transaction.builder()
                                .offset(offset)
                                .timestamp((oldValue.getTimestamp().compareTo(newValue.getTimestamp()) > 0) ? oldValue.getTimestamp() : newValue.getTimestamp())
                                .amount(newValue.getAmount().add(oldValue.getAmount()))
                                .count(oldValue.getCount() + newValue.getCount())
                                .max(TransactionUtil.max(oldValue.getMax(), newValue.getMax()))
                                .min(TransactionUtil.min(oldValue.getMin(), newValue.getMin()))
                                .build();
                    }
                });

        return transactionDtoConverter.convert(merge);
    }


    public SummaryStatisticDto getTransactionStatistics(final Instant instantNow) {
        TransactionSummaryStatistics bds = store.values().stream()
                .parallel()
                .filter(t -> TransactionUtil.isOffsetBeforeOrEqual(t.getTimestamp(), instantNow, MAX_OFFSET))
                .collect(TransactionSummaryStatistics.statistics());

        return summaryStatisticsConverter.convert(bds);
    }

    public void cleanTransactionStatistics() {
        LongStream.rangeClosed(0, MAX_OFFSET)
                .forEach(i -> store.compute(i, (key, value) -> getValue(i)));
    }

    /**
     * For testing only.
     * Never use size() with concurrent collections in prod,
     * in turn make use of isEmpty().
     *
     * @return
     */
    int getStoreSize() {
        return store.size();
    }

    public int getMaxOffset() {
        return MAX_OFFSET;
    }

    @Value("${max-offset}")
    public void setMaxOffset(int lastMilli) {
        MAX_OFFSET = lastMilli;
    }
}
