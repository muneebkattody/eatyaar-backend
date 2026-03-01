package com.eatyaar.util;

import com.eatyaar.exception.OtpMaxAttemptsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class OtpStore {

    private static final int    OTP_EXPIRY_SECONDS = 300; // 5 minutes
    private static final int    MAX_ATTEMPTS        = 3;

    // phone → OtpEntry
    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    private record OtpEntry(
        String otp,
        Instant expiresAt,
        int attempts          // wrong attempts so far
    ) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
        boolean isMaxAttemptsReached() {
            return attempts >= MAX_ATTEMPTS;
        }
        OtpEntry incrementAttempts() {
            return new OtpEntry(otp, expiresAt, attempts + 1);
        }
    }

    // ── Save (or replace) OTP for a phone ───────────────────────
    public void saveOtp(String phone, String otp) {
        store.put(phone, new OtpEntry(
            otp,
            Instant.now().plusSeconds(OTP_EXPIRY_SECONDS),
            0
        ));
        log.info("OTP saved for phone: {}***", phone.substring(0, 5));
    }

    // ── Verify OTP — returns true/false, throws on max attempts ──
    public boolean verifyOtp(String phone, String otp) {
        OtpEntry entry = store.get(phone);

        if (entry == null) {
            log.warn("OTP verification attempt for unknown phone: {}***", phone.substring(0, 5));
            return false;
        }

        // Expired
        if (entry.isExpired()) {
            store.remove(phone);
            log.warn("Expired OTP used for phone: {}***", phone.substring(0, 5));
            return false;
        }

        // Too many wrong attempts
        if (entry.isMaxAttemptsReached()) {
            store.remove(phone);
            log.warn("Max OTP attempts exceeded for phone: {}***", phone.substring(0, 5));
            throw new OtpMaxAttemptsException();
        }

        // Wrong OTP — increment attempt counter
        if (!entry.otp().equals(otp)) {
            store.put(phone, entry.incrementAttempts());
            int remaining = MAX_ATTEMPTS - (entry.attempts() + 1);
            log.warn("Wrong OTP for phone {}***. Attempts remaining: {}", phone.substring(0, 5), remaining);
            return false;
        }

        // Correct ✅
        log.info("OTP verified successfully for phone: {}***", phone.substring(0, 5));
        return true;
    }

    // ── Clear OTP after successful verify ────────────────────────
    public void clearOtp(String phone) {
        store.remove(phone);
    }
}
