package com.talenttrade.backend.exception;

/**
 * Thrown for structurally invalid rating attempts:
 * - rating a trade that isn't COMPLETED yet
 * - the ratedUserId doesn't match the other participant in the trade
 * - a user trying to rate themselves
 */
public class InvalidRatingException extends RuntimeException {

    public InvalidRatingException(String message) {
        super(message);
    }
}