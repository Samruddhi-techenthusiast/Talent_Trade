package com.talenttrade.backend.exception;

/**
 * Thrown for invalid trade operations such as:
 * - sending a trade request to yourself
 * - offering a skill that isn't yours
 * - requesting a skill the receiver doesn't have
 * - duplicate active trade requests between the same two users
 * - invalid status transitions (e.g. PENDING -> COMPLETED directly)
 */
public class InvalidTradeException extends RuntimeException {

    public InvalidTradeException(String message) {
        super(message);
    }
}
