package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends EatYaarException {
    public InvalidOtpException() {
        super("Invalid or expired OTP.", HttpStatus.BAD_REQUEST);
    }
}