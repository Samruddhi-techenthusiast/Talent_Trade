package com.talenttrade.backend.exception;

/**
 * Thrown when a user attempts to perform a trade action they are not
 * authorized to perform — e.g. someone other than the receiver tries
 * to accept/reject a trade, or someone other than a participant tries
 * to view/complete it.
 */
public class UnauthorizedTradeActionException extends RuntimeException {

    public UnauthorizedTradeActionException(String message) {
        super(message);
    }
}
