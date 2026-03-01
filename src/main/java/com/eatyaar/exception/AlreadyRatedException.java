package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

class AlreadyRatedException extends EatYaarException {
    public AlreadyRatedException() {
        super("You have already rated this listing.", HttpStatus.CONFLICT);
    }
}