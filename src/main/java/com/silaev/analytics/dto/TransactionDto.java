package com.silaev.analytics.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

/**
 * Dto for Transaction
 */
@Data
@Builder
@AllArgsConstructor
public class TransactionDto {
    @JsonProperty("amount")
    @NotNull
    private BigDecimal amount;

    /**
     * The folowwing are not represented in a response,
     * but used to test and verify TransactionDto
     */
    @JsonIgnore
    private final Long count;
    @JsonIgnore
    private final BigDecimal min;
    @JsonIgnore
    private final BigDecimal max;
    @JsonIgnore
    private final Long offset; //represents a millisecond
    //converts to UTC
    //2018-10-22T07:13:05.992-04:00[America/Montreal] -> 2018-10-22T11:13:05.992Z[UTC]
    @JsonProperty("timestamp")
    @NotNull
    private ZonedDateTime zonedDateTime;
}
