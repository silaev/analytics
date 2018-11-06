package com.silaev.analytics.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.temporal.Temporal;

public class TransactionUtil {

    public static final int SCALE = 2;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    public static final BigDecimal BIG_DECIMAL_ZERO = setScaleAndRound(BigDecimal.ZERO);

    public static long getOffset(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
        return Duration.between(temporal1Inclusive, temporal2Exclusive).toMillis();
    }

    public static boolean isOffsetAfter(Temporal temporal1Inclusive, Temporal temporal2Exclusive, int lastMillisecond) {
        return Duration.between(temporal1Inclusive, temporal2Exclusive).toMillis() > lastMillisecond;
    }

    public static boolean isOffsetBeforeOrEqual(Temporal temporal1Inclusive, Temporal temporal2Exclusive, int lastMillisecond) {
        return Duration.between(temporal1Inclusive, temporal2Exclusive).toMillis() <= lastMillisecond;
    }

    public static BigDecimal setScaleAndRound(BigDecimal value) {
        return value.setScale(SCALE, ROUNDING_MODE);
    }

    public static BigDecimal max(BigDecimal oldValue, BigDecimal newValue) {
        return (oldValue.compareTo(newValue) > 0) ? oldValue : newValue;
    }

    public static BigDecimal min(BigDecimal oldValue, BigDecimal newValue) {
        return (oldValue.compareTo(newValue) < 0) ? oldValue : newValue;
    }
}
