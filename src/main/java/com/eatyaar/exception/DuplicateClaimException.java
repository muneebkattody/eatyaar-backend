package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

class DuplicateClaimException extends EatYaarException {
    public DuplicateClaimException() {
        super("You have already claimed this listing.", HttpStatus.CONFLICT);
    }
}