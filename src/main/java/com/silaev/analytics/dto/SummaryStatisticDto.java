package com.silaev.analytics.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Dto for summary statistic to represent as a response.
 */
@Data
@Builder
public class SummaryStatisticDto {
    private String sum;
    private String avg;
    private String max;
    private String min;
    private Long count;
}
