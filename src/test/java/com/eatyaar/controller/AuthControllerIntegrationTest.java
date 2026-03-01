package com.eatyaar.controller;

import com.eatyaar.dto.request.SendOtpRequest;
import com.eatyaar.dto.request.VerifyOtpRequest;
import com.eatyaar.service.AuthService;
import com.eatyaar.dto.response.AuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Auth API Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/send-otp: valid phone returns 200")
    void sendOtp_validPhone_returns200() throws Exception {
        SendOtpRequest request = new SendOtpRequest();
        request.setPhone("9876543210");

        doNothing().when(authService).sendOtp(any());

        mockMvc.perform(post("/api/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("9876543210")));
    }

    @Test
    @DisplayName("POST /api/auth/send-otp: invalid phone returns 400")
    void sendOtp_invalidPhone_returns400() throws Exception {
        SendOtpRequest request = new SendOtpRequest();
        request.setPhone("123"); // invalid — not 10 digits

        mockMvc.perform(post("/api/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/send-otp: missing phone returns 400")
    void sendOtp_missingPhone_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/verify-otp: valid request returns token")
    void verifyOtp_validRequest_returnsToken() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setPhone("9876543210");
        request.setOtp("123456");

        AuthResponse authResponse = new AuthResponse("mock.jwt.token", 1L, "9876543210", true);
        when(authService.verifyOtp(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.phone").value("9876543210"))
                .andExpect(jsonPath("$.newUser").value(true));
    }

    @Test
    @DisplayName("POST /api/auth/verify-otp: missing fields returns 400")
    void verifyOtp_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\": \"9876543210\"}")) // missing otp
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/auth/profile: unauthenticated returns 403")
    void completeProfile_unauthenticated_returns403() throws Exception {
        mockMvc.perform(patch("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"city\":\"Pune\",\"area\":\"Kothrud\"}"))
                .andExpect(status().isForbidden());
    }
}