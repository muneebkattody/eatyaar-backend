package com.eatyaar.service;

import com.eatyaar.dto.request.CompleteProfileRequest;
import com.eatyaar.dto.request.SendOtpRequest;
import com.eatyaar.dto.request.VerifyOtpRequest;
import com.eatyaar.dto.response.AuthResponse;
import com.eatyaar.entity.User;
import com.eatyaar.repository.UserRepository;
import com.eatyaar.util.JwtUtil;
import com.eatyaar.util.OtpStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpStore otpStore;
    @Mock private JwtUtil jwtUtil;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_PHONE = "9876543210";
    private static final String TEST_OTP   = "123456";
    private static final String TEST_TOKEN = "jwt.test.token";

    // ─── sendOtp ────────────────────────────────────────────────

    @Test
    @DisplayName("sendOtp: saves OTP and sends email")
    void sendOtp_savesAndSendsEmail() {
        SendOtpRequest request = new SendOtpRequest();
        request.setPhone(TEST_PHONE);

        authService.sendOtp(request);

        verify(otpStore, times(1)).saveOtp(eq(TEST_PHONE), anyString());
        verify(emailService, times(1)).sendOtp(eq(TEST_PHONE), anyString());
    }

    @Test
    @DisplayName("sendOtp: generates 6-digit OTP")
    void sendOtp_generatesSixDigitOtp() {
        SendOtpRequest request = new SendOtpRequest();
        request.setPhone(TEST_PHONE);

        authService.sendOtp(request);

        verify(otpStore).saveOtp(eq(TEST_PHONE), argThat(otp ->
                otp.length() == 6 && otp.matches("\\d{6}")
        ));
    }

    // ─── verifyOtp ──────────────────────────────────────────────

    @Test
    @DisplayName("verifyOtp: new user — creates account and returns isNewUser=true")
    void verifyOtp_newUser_createsAccount() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone(TEST_PHONE);
        request.setOtp(TEST_OTP);

        when(otpStore.verifyOtp(TEST_PHONE, TEST_OTP)).thenReturn(true);
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn(TEST_TOKEN);

        AuthResponse response = authService.verifyOtp(request);

        assertThat(response.isNewUser()).isTrue();
        assertThat(response.getToken()).isEqualTo(TEST_TOKEN);
        assertThat(response.getPhone()).isEqualTo(TEST_PHONE);
        verify(userRepository).save(any(User.class));
        verify(otpStore).clearOtp(TEST_PHONE);
    }

    @Test
    @DisplayName("verifyOtp: existing user — does not create account, returns isNewUser=false")
    void verifyOtp_existingUser_doesNotCreateAccount() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone(TEST_PHONE);
        request.setOtp(TEST_OTP);

        User existingUser = User.builder().id(1L).phone(TEST_PHONE).name("Test User").isVerified(true).build();

        when(otpStore.verifyOtp(TEST_PHONE, TEST_OTP)).thenReturn(true);
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(Optional.of(existingUser));
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn(TEST_TOKEN);

        AuthResponse response = authService.verifyOtp(request);

        assertThat(response.isNewUser()).isFalse();
        assertThat(response.getUserId()).isEqualTo(1L);
        verify(userRepository, never()).save(any()); // should NOT create new user
    }

    @Test
    @DisplayName("verifyOtp: invalid OTP — throws exception")
    void verifyOtp_invalidOtp_throwsException() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone(TEST_PHONE);
        request.setOtp("000000");

        when(otpStore.verifyOtp(TEST_PHONE, "000000")).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid or expired OTP");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("verifyOtp: clears OTP after successful verification")
    void verifyOtp_clearsOtpAfterSuccess() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone(TEST_PHONE);
        request.setOtp(TEST_OTP);

        User user = User.builder().id(1L).phone(TEST_PHONE).isVerified(true).build();
        when(otpStore.verifyOtp(TEST_PHONE, TEST_OTP)).thenReturn(true);
        when(userRepository.existsByPhone(TEST_PHONE)).thenReturn(true);
        when(userRepository.findByPhone(TEST_PHONE)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn(TEST_TOKEN);

        authService.verifyOtp(request);

        verify(otpStore).clearOtp(TEST_PHONE); // must be cleared
    }

    // ─── completeProfile ────────────────────────────────────────

    @Test
    @DisplayName("completeProfile: updates user fields correctly")
    void completeProfile_updatesFields() {
        User user = User.builder().id(1L).phone(TEST_PHONE).build();
        CompleteProfileRequest request = new CompleteProfileRequest();
        request.setName("Rahul");
        request.setCity("Pune");
        request.setArea("Koregaon Park");
        request.setEmail("rahul@gmail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = authService.completeProfile(1L, request);

        assertThat(updated.getName()).isEqualTo("Rahul");
        assertThat(updated.getCity()).isEqualTo("Pune");
        assertThat(updated.getArea()).isEqualTo("Koregaon Park");
        assertThat(updated.getEmail()).isEqualTo("rahul@gmail.com");
    }

    @Test
    @DisplayName("completeProfile: throws when user not found")
    void completeProfile_userNotFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.completeProfile(99L, new CompleteProfileRequest()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }
}