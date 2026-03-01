package com.eatyaar.service;

import com.eatyaar.dto.request.CompleteProfileRequest;
import com.eatyaar.dto.request.SendOtpRequest;
import com.eatyaar.dto.request.VerifyOtpRequest;
import com.eatyaar.dto.response.AuthResponse;
import com.eatyaar.entity.User;
import com.eatyaar.exception.InvalidOtpException;
import com.eatyaar.exception.ResourceNotFoundException;
import com.eatyaar.repository.UserRepository;
import com.eatyaar.util.JwtUtil;
import com.eatyaar.util.OtpStore;
import com.eatyaar.util.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RateLimiter rateLimiter;

    public void sendOtp(SendOtpRequest request) {
        String phone = request.getPhone();

        // Rate limit: max 3 OTPs per 10 minutes per phone
        rateLimiter.checkLimit(phone);

        String otp = generateOtp();
        otpStore.saveOtp(phone, otp);
        emailService.sendOtp(phone, otp);

        log.info("OTP sent for phone: {}***", phone.substring(0, 5));
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone();
        boolean valid = otpStore.verifyOtp(phone, request.getOtp());

        if (!valid) {
            throw new InvalidOtpException();
        }

        otpStore.clearOtp(phone);

        boolean isNewUser = !userRepository.existsByPhone(phone);
        User user;

        if (isNewUser) {
            user = User.builder()
                    .phone(phone)
                    .name("")
                    .isVerified(true)
                    .trustScore(0.0)
                    .totalGiven(0)
                    .totalTaken(0)
                    .build();
            userRepository.save(user);
            log.info("New user registered: {}***", phone.substring(0, 5));
        } else {
            user = userRepository.findByPhone(phone)
                    .orElseThrow(() -> new ResourceNotFoundException("User"));
            log.info("Existing user logged in: {}***", phone.substring(0, 5));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new AuthResponse(token, user.getId(), user.getPhone(), isNewUser);
    }

    public User completeProfile(Long userId, CompleteProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCity(request.getCity());
        user.setArea(request.getArea());

        log.info("Profile completed for userId: {}", userId);
        return userRepository.save(user);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
