package com.eatyaar.controller;

import com.eatyaar.dto.request.CompleteProfileRequest;
import com.eatyaar.dto.request.SendOtpRequest;
import com.eatyaar.dto.request.VerifyOtpRequest;
import com.eatyaar.dto.response.AuthResponse;
import com.eatyaar.entity.User;
import com.eatyaar.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Send OTP to phone number")
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok("OTP sent to " + request.getPhone());
    }

    // Step 2 — Verify OTP and get JWT token
    @Operation(summary = "Verify OTP and receive JWT token")
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    // Step 3 — Complete profile (name, city, area)
    @PatchMapping("/profile")
    public ResponseEntity<User> completeProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CompleteProfileRequest request) {
        User updated = authService.completeProfile(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }
}
