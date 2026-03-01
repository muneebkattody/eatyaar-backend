package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

class UnauthorizedActionException extends EatYaarException {
    public UnauthorizedActionException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
