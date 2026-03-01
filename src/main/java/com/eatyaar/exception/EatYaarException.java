package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

public class EatYaarException extends RuntimeException {
    private final HttpStatus status;

    public EatYaarException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}