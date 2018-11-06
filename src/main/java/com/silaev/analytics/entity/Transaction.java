package com.silaev.analytics.entity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable class for multithreading usage
 */
@Data
@Builder
public final class Transaction {
    private final Long offset; //represents a millisecond

    private final Instant timestamp;

    private final BigDecimal amount;

    private final Long count;

    private final BigDecimal min;

    private final BigDecimal max;
}
