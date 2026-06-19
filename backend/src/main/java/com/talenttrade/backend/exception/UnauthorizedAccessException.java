package com.talenttrade.backend.exception;

/**
 * Generic exception for "you don't own this resource" violations that
 * aren't specific to trades (e.g. trying to mark another user's
 * notification as read). Distinct from UnauthorizedTradeActionException,
 * which is scoped to the Trade module.
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }
}
