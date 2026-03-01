package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

class SelfClaimException extends EatYaarException {
    public SelfClaimException() {
        super("You cannot claim your own listing.", HttpStatus.BAD_REQUEST);
    }
}
