package com.example.demo.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
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

/**
 * Centralized Spring MVC exception translation layer. Converts thrown exceptions into consistent
 * JSON payloads while logging with appropriate severity. Keeps controllers thin and ensures clients
 * can rely on uniform error structure.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getAllErrors()
                .forEach(
                        (error) -> { // aggregate field errors into map
                            String fieldName = ((FieldError) error).getField();
                            String errorMessage = error.getDefaultMessage();
                            errors.put(fieldName, errorMessage);
                        });

        logger.warn("Validation failed for request: {}, errors: {}", request.getDescription(false), errors);

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), "Validation failed", request.getDescription(false), "VALIDATION_ERROR", errors);

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorDetails> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {

        logger.warn("Authentication failed for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), "Invalid credentials", request.getDescription(false), "AUTHENTICATION_FAILED");

        return new ResponseEntity<>(errorDetails, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

        logger.warn("Access denied for request: {}", request.getDescription(false));

        ErrorDetails errorDetails =
                new ErrorDetails(LocalDateTime.now(), "Access denied", request.getDescription(false), "ACCESS_DENIED");

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
                "CIRCUIT_BREAKER_OPEN");

        return new ResponseEntity<>(errorDetails, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorDetails> handleRateLimitException(RequestNotPermitted ex, WebRequest request) {

        logger.warn("Rate limit exceeded for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), "Rate limit exceeded", request.getDescription(false), "RATE_LIMIT_EXCEEDED");

        return new ResponseEntity<>(errorDetails, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorDetails> handleTimeoutException(TimeoutException ex, WebRequest request) {

        logger.warn("Request timeout for: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), "Request timeout", request.getDescription(false), "REQUEST_TIMEOUT");

        return new ResponseEntity<>(errorDetails, HttpStatus.REQUEST_TIMEOUT);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDetails> handleDataAccessException(DataAccessException ex, WebRequest request) {

        logger.error("Database error for request: {}", request.getDescription(false), ex);

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), "Database operation failed", request.getDescription(false), "DATABASE_ERROR");

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomCheckedException.class)
    public ResponseEntity<ErrorDetails> handleCustomCheckedException(CustomCheckedException ex, WebRequest request) {

        logger.warn("Business logic error: {}", ex.getMessage());

        ErrorDetails errorDetails =
                new ErrorDetails(LocalDateTime.now(), ex.getMessage(), request.getDescription(false), "BUSINESS_ERROR");

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle UserNotFoundException (404 NOT_FOUND)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleUserNotFound(UserNotFoundException ex, WebRequest request) {
        logger.warn("User not found for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getErrorCode());

        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle DuplicateEmailException (409 CONFLICT)
     */
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorDetails> handleDuplicateEmail(DuplicateEmailException ex, WebRequest request) {
        logger.warn("Duplicate email for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getErrorCode());

        return new ResponseEntity<>(errorDetails, HttpStatus.CONFLICT);
    }

    /**
     * Handle InsufficientPrivilegesException (403 FORBIDDEN)
     */
    @ExceptionHandler(InsufficientPrivilegesException.class)
    public ResponseEntity<ErrorDetails> handleInsufficientPrivileges(
            InsufficientPrivilegesException ex, WebRequest request) {
        logger.warn("Insufficient privileges for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getErrorCode());

        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle InvalidStateTransitionException (422 UNPROCESSABLE_ENTITY)
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ErrorDetails> handleInvalidStateTransition(
            InvalidStateTransitionException ex, WebRequest request) {
        logger.warn("Invalid state transition for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getErrorCode());

        return new ResponseEntity<>(errorDetails, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Generic DomainException handler (catches any subclass not specifically handled above)
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorDetails> handleDomainException(DomainException ex, WebRequest request) {
        logger.warn("Domain error for request: {}", request.getDescription(false));

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), ex.getMessage(), request.getDescription(false), ex.getErrorCode());

        HttpStatus status = HttpStatus.resolve(ex.getHttpStatus());
        return new ResponseEntity<>(errorDetails, status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class) // fallback catch-all – always keep last to avoid pre-emption
    public ResponseEntity<ErrorDetails> handleGlobalException(Exception ex, WebRequest request) {
        logger.error("Unexpected error for request: {}", request.getDescription(false), ex);

        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(), "An unexpected error occurred", request.getDescription(false), "INTERNAL_ERROR");

        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

/**
 * Simple POJO serialized as JSON describing an error. Exposed as a nested class to scope it to the
 * handler (could also live in a shared error package if reused).
 */
class ErrorDetails {
    private LocalDateTime timestamp; // when the error response was generated
    private String message; // human-readable summary
    private String details; // request description (URI context)
    private String errorCode; // machine-readable code
    private Map<String, String> validationErrors; // optional map of field -> message for validation failures

    public ErrorDetails(LocalDateTime timestamp, String message, String details, String errorCode) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
        this.errorCode = errorCode;
    }

    public ErrorDetails(
            LocalDateTime timestamp,
            String message,
            String details,
            String errorCode,
            Map<String, String> validationErrors) {
        this(timestamp, message, details, errorCode); // delegate to main constructor
        this.validationErrors = validationErrors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
