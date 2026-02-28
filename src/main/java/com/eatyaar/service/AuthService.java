package com.eatyaar.service;

import com.eatyaar.dto.request.CompleteProfileRequest;
import com.eatyaar.dto.request.VerifyOtpRequest;
import com.eatyaar.dto.response.AuthResponse;
import com.eatyaar.entity.User;
import com.eatyaar.repository.UserRepository;
import com.eatyaar.util.JwtUtil;
import com.eatyaar.util.OtpStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OtpStore otpStore;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public void sendOtp(CompleteProfileRequest.SendOtpRequest request) {
        String otp = generateOtp();
        otpStore.saveOtp(request.getPhone(), otp);

        // TODO: Integrate Fast2SMS here in production
        // For now, print OTP to console for development/testing
        System.out.println("=============================");
        System.out.println("OTP for " + request.getPhone() + " : " + otp);
        System.out.println("=============================");
        emailService.sendOtp(request.getPhone(), otp);
    }

    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        boolean valid = otpStore.verifyOtp(request.getPhone(), request.getOtp());
        if (!valid) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        otpStore.clearOtp(request.getPhone());

        // Check if user exists — if not, create a new one
        boolean isNewUser = !userRepository.existsByPhone(request.getPhone());

        User user;
        if (isNewUser) {
            user = User.builder()
                    .phone(request.getPhone())
                    .name("") // will be filled in profile completion
                    .isVerified(true)
                    .build();
            userRepository.save(user);
        } else {
            user = userRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());
        return new AuthResponse(token, user.getId(), user.getPhone(), isNewUser);
    }

    public User completeProfile(Long userId, CompleteProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setCity(request.getCity());
        user.setArea(request.getArea());

        return userRepository.save(user);
    }

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
