package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

public class OtpMaxAttemptsException extends EatYaarException {
    public OtpMaxAttemptsException() {
        super("Too many wrong attempts. Please request a new OTP.", HttpStatus.BAD_REQUEST);
    }
}
