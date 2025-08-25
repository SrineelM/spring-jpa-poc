package com.example.demo.exception;

/**
 * Enhanced custom exception for business logic errors with error codes and context
 */
public class CustomCheckedException extends Exception {

    private final String errorCode;
    private final Object context;

    public CustomCheckedException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.context = null;
    }

    public CustomCheckedException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.context = null;
    }

    public CustomCheckedException(String message, String errorCode, Object context) {
        super(message);
        this.errorCode = errorCode;
        this.context = context;
    }

    public CustomCheckedException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.context = null;
    }

    public CustomCheckedException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.context = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object getContext() {
        return context;
    }
}
