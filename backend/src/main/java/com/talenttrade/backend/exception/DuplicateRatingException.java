package com.talenttrade.backend.exception;

/**
 * Thrown when a user attempts to rate the same trade more than once.
 */
public class DuplicateRatingException extends RuntimeException {

    public DuplicateRatingException(String message) {
        super(message);
    }
}
