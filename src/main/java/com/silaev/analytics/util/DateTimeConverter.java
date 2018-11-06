package com.silaev.analytics.util;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DateTimeConverter {

    public Instant getInstantNow() {
        return Instant.now();
    }
}
