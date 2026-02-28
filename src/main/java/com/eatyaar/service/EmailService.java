// ── EmailService.java ────────────────────────────────────────────
package com.eatyaar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${mail.test.recipient}")
    private String testRecipient; // your inbox where OTPs land

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtp(String phone, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(testRecipient);
        message.setSubject("EatYaar OTP - " + phone);
        message.setText(
                "Hello,\n\n" +
                        "OTP requested for phone number: " + phone + "\n\n" +
                        "Your OTP is: " + otp + "\n\n" +
                        "This OTP is valid for 5 minutes.\n\n" +
                        "— EatYaar Team"
        );
        mailSender.send(message);
    }
}