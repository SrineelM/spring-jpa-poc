package com.example.demo.exception;

/**
 * Thrown when a user is not found (404 NOT_FOUND).
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(Long id) {
        super(String.format("User with ID %d not found", id), "USER_NOT_FOUND", 404);
    }

    public UserNotFoundException(String email) {
        super(String.format("User with email '%s' not found", email), "USER_NOT_FOUND", 404);
    }
}
