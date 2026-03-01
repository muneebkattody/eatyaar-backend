package com.eatyaar.exception;

import org.springframework.http.HttpStatus;

class ListingNotAvailableException extends EatYaarException {
    public ListingNotAvailableException() {
        super("This listing is no longer available.", HttpStatus.GONE);
    }
}
