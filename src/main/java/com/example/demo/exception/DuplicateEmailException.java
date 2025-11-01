package com.example.demo.exception;

/**
 * Thrown when attempting to register or update with an email already in use (409 CONFLICT).
 */
public class DuplicateEmailException extends DomainException {

    public DuplicateEmailException(String email) {
        super(String.format("Email '%s' is already registered", email), "DUPLICATE_EMAIL", 409);
    }
}
