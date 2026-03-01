package com.eatyaar.util;

import com.eatyaar.exception.RateLimitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter.
 * Limits how many OTP requests can be made per phone number per time window.
 * Production note: replace with Redis-backed solution for multi-instance deploys.
 */
@Slf4j
@Component
public class RateLimiter {

    private static final int    MAX_REQUESTS     = 3;    // max OTP sends per window
    private static final long   WINDOW_SECONDS   = 600;  // 10-minute window

    private record RequestRecord(int count, Instant windowStart) {
        boolean isWindowExpired() {
            return Instant.now().isAfter(windowStart.plusSeconds(WINDOW_SECONDS));
        }
        RequestRecord increment() {
            return new RequestRecord(count + 1, windowStart);
        }
        boolean isLimitReached() {
            return count >= MAX_REQUESTS;
        }
    }

    private final ConcurrentHashMap<String, RequestRecord> store = new ConcurrentHashMap<>();

    /**
     * Call before sending OTP.
     * Throws RateLimitException if the phone has exceeded the limit.
     * Key can be phone number or IP address.
     */
    public void checkLimit(String key) {
        store.compute(key, (k, existing) -> {
            // No record or window expired → fresh start
            if (existing == null || existing.isWindowExpired()) {
                return new RequestRecord(1, Instant.now());
            }

            // Limit reached within window → throw
            if (existing.isLimitReached()) {
                log.warn("Rate limit hit for key: {}***", key.substring(0, Math.min(5, key.length())));
                throw new RateLimitException();
            }

            // Increment
            return existing.increment();
        });
    }
}
