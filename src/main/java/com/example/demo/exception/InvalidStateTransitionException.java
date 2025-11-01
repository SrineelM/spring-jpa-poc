package com.example.demo.exception;

/**
 * Thrown when an invalid state transition is attempted (422 UNPROCESSABLE_ENTITY).
 */
public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String from, String to) {
        super(String.format("Cannot transition from '%s' to '%s'", from, to), "INVALID_STATE_TRANSITION", 422);
    }
}
