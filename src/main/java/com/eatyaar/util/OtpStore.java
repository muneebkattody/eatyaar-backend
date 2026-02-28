package com.eatyaar.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OtpStore {

    // Stores phone → OtpEntry (otp + expiry time)
    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    // Save OTP for a phone number
    public void saveOtp(String phone, String otp) {
        store.put(phone, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
    }

    // Verify OTP — returns true if correct and not expired
    public boolean verifyOtp(String phone, String otp) {
        OtpEntry entry = store.get(phone);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            store.remove(phone); // cleanup expired
            return false;
        }
        return entry.otp().equals(otp);
    }

    // Remove OTP after successful verification
    public void clearOtp(String phone) {
        store.remove(phone);
    }

    // Internal record to hold OTP + expiry
    private record OtpEntry(String otp, LocalDateTime expiresAt) {}
}
