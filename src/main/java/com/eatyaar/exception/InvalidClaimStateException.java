package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

class InvalidClaimStateException extends EatYaarException {
    public InvalidClaimStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
