package com.example.demo.exception;

/**
 * Thrown when user lacks required permissions (403 FORBIDDEN).
 */
public class InsufficientPrivilegesException extends DomainException {

    public InsufficientPrivilegesException(String operation) {
        super(String.format("Insufficient privileges for operation: %s", operation), "INSUFFICIENT_PRIVILEGES", 403);
    }
}
