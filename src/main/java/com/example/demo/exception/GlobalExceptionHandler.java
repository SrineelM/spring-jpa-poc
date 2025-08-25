package com.example.demo.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Centralized exception handling with comprehensive error mapping and structured logging
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation failed for request: {}, errors: {}", request.getDescription(false), errors);
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Validation failed",
            request.getDescription(false),
            "VALIDATION_ERROR",
            errors
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        
        logger.warn("Authentication failed for request: {}", request.getDescription(false));
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Invalid credentials",
            request.getDescription(false),
            "AUTHENTICATION_FAILED"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        logger.warn("Access denied for request: {}", request.getDescription(false));
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Access denied",
            request.getDescription(false),
            "ACCESS_DENIED"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorDetails> handleCircuitBreakerException(
            CallNotPermittedException ex, WebRequest request) {
        
        logger.warn("Circuit breaker is open for request: {}", request.getDescription(false));
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Service temporarily unavailable - circuit breaker is open",
            request.getDescription(false),
            "CIRCUIT_BREAKER_OPEN"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorDetails> handleRateLimitException(
            RequestNotPermitted ex, WebRequest request) {
        
        logger.warn("Rate limit exceeded for request: {}", request.getDescription(false));
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Rate limit exceeded",
            request.getDescription(false),
            "RATE_LIMIT_EXCEEDED"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorDetails> handleTimeoutException(
            TimeoutException ex, WebRequest request) {
        
        logger.warn("Request timeout for: {}", request.getDescription(false));
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Request timeout",
            request.getDescription(false),
            "REQUEST_TIMEOUT"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDetails> handleDataAccessException(
            DataAccessException ex, WebRequest request) {
        
        logger.error("Database error for request: {}", request.getDescription(false), ex);
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "Database operation failed",
            request.getDescription(false),
            "DATABASE_ERROR"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomCheckedException.class)
    public ResponseEntity<ErrorDetails> handleCustomCheckedException(
            CustomCheckedException ex, WebRequest request) {
        
        logger.warn("Business logic error: {}", ex.getMessage());
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            ex.getMessage(),
            request.getDescription(false),
            "BUSINESS_ERROR"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error for request: {}", request.getDescription(false), ex);
        
        ErrorDetails errorDetails = new ErrorDetails(
            LocalDateTime.now(),
            "An unexpected error occurred",
            request.getDescription(false),
            "INTERNAL_ERROR"
        );
        
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

/**
 * Enhanced error details with error codes and additional metadata
 */
class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private String details;
    private String errorCode;
    private Map<String, String> validationErrors;

    public ErrorDetails(LocalDateTime timestamp, String message, String details, String errorCode) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.errorCode = errorCode;
    }

    public ErrorDetails(LocalDateTime timestamp, String message, String details, String errorCode, Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.errorCode = errorCode;
        this.validationErrors = validationErrors;
    }

    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
    public String getErrorCode() { return errorCode; }
    public Map<String, String> getValidationErrors() { return validationErrors; }
}
