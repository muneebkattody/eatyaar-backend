package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

public class RateLimitException extends EatYaarException {
    public RateLimitException() {
        super("Too many requests. Please wait before trying again.", HttpStatus.TOO_MANY_REQUESTS);
    }
}