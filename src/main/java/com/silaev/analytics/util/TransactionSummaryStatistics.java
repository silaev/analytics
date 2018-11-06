package com.silaev.analytics.util;

import com.silaev.analytics.entity.Transaction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;

@Data
@Slf4j
public class TransactionSummaryStatistics implements Consumer<Transaction> {

    private BigDecimal sum = TransactionUtil.BIG_DECIMAL_ZERO;
    private BigDecimal min = TransactionUtil.BIG_DECIMAL_ZERO;
    private BigDecimal max = TransactionUtil.BIG_DECIMAL_ZERO;
    private long count;

    public static Collector<Transaction, ?, TransactionSummaryStatistics> statistics() {
        return Collector.of(TransactionSummaryStatistics::new,
                TransactionSummaryStatistics::accept,
                TransactionSummaryStatistics::merge,
                Collector.Characteristics.UNORDERED);
    }

    public void accept(Transaction t) {
        if (count == 0) {
            Objects.requireNonNull(t);
            count = t.getCount();
            sum = t.getAmount();
            min = t.getMin();
            max = t.getMax();
        } else {
            sum = TransactionUtil.setScaleAndRound(sum.add(t.getAmount()));
            if (min.compareTo(t.getMin()) > 0) min = t.getMin();
            if (max.compareTo(t.getMax()) < 0) max = t.getMax();
            count = count + t.getCount();
        }
    }

    private TransactionSummaryStatistics merge(TransactionSummaryStatistics s) {
        if (s.count > 0) {
            if (count == 0) {
                count = s.count;
                sum = s.sum;
                min = s.min;
                max = s.max;
            } else {
                sum = TransactionUtil.setScaleAndRound(sum.add(s.sum));
                if (min.compareTo(s.min) > 0) min = s.min;
                if (max.compareTo(s.max) < 0) max = s.max;
                count += s.count;
            }
        }
        return this;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public BigDecimal getAverage(MathContext mc) {
        return count < 2 ? sum :
                TransactionUtil.setScaleAndRound(sum.divide(BigDecimal.valueOf(count), mc));
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }

    @Override
    public String toString() {
        return count == 0 ? "empty" : String.format("%d elements between %s and %s, sum=%s", count, format(min), format(max), format(sum));
    }

    private String format(BigDecimal bd) {
        return new DecimalFormat("0.00").format(bd);
    }
}
