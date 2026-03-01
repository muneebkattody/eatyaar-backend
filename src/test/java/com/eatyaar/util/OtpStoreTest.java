package com.eatyaar.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("OtpStore Tests")
class OtpStoreTest {

    private OtpStore otpStore;

    @BeforeEach
    void setUp() {
        otpStore = new OtpStore();
    }

    @Test
    @DisplayName("saveAndVerify: correct OTP returns true")
    void verifyOtp_correctOtp_returnsTrue() {
        otpStore.saveOtp("9876543210", "123456");
        assertThat(otpStore.verifyOtp("9876543210", "123456")).isTrue();
    }

    @Test
    @DisplayName("verifyOtp: wrong OTP returns false")
    void verifyOtp_wrongOtp_returnsFalse() {
        otpStore.saveOtp("9876543210", "123456");
        assertThat(otpStore.verifyOtp("9876543210", "999999")).isFalse();
    }

    @Test
    @DisplayName("verifyOtp: unknown phone returns false")
    void verifyOtp_unknownPhone_returnsFalse() {
        assertThat(otpStore.verifyOtp("0000000000", "123456")).isFalse();
    }

    @Test
    @DisplayName("clearOtp: after clear, OTP is invalid")
    void clearOtp_invalidatesOtp() {
        otpStore.saveOtp("9876543210", "123456");
        otpStore.clearOtp("9876543210");
        assertThat(otpStore.verifyOtp("9876543210", "123456")).isFalse();
    }

    @Test
    @DisplayName("saveOtp: second save overwrites first")
    void saveOtp_overwritesPrevious() {
        otpStore.saveOtp("9876543210", "111111");
        otpStore.saveOtp("9876543210", "222222");
        assertThat(otpStore.verifyOtp("9876543210", "111111")).isFalse();
        assertThat(otpStore.verifyOtp("9876543210", "222222")).isTrue();
    }

    @Test
    @DisplayName("verifyOtp: different phones are independent")
    void verifyOtp_phonesAreIndependent() {
        otpStore.saveOtp("9000000001", "111111");
        otpStore.saveOtp("9000000002", "222222");
        assertThat(otpStore.verifyOtp("9000000001", "222222")).isFalse();
        assertThat(otpStore.verifyOtp("9000000002", "111111")).isFalse();
        assertThat(otpStore.verifyOtp("9000000001", "111111")).isTrue();
        assertThat(otpStore.verifyOtp("9000000002", "222222")).isTrue();
    }
}