package com.example.demo.exception;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

/**
 * Unit tests for domain exception hierarchy.
 * Verifies exception types and HTTP status code mappings.
 */
@DisplayName("Domain Exception Tests")
class DomainExceptionTest {

    @Test
    @DisplayName("UserNotFoundException should have 404 status")
    void testUserNotFoundException_Http404() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException(123L);

        // Assert
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(exception.getErrorCode()).isEqualTo("USER_NOT_FOUND");
        assertThat(exception.getMessage()).contains("123");
    }

    @Test
    @DisplayName("DuplicateEmailException should have 409 status")
    void testDuplicateEmailException_Http409() {
        // Arrange
        DuplicateEmailException exception = new DuplicateEmailException("test@example.com");

        // Assert
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(exception.getErrorCode()).isEqualTo("DUPLICATE_EMAIL");
        assertThat(exception.getMessage()).contains("test@example.com");
    }

    @Test
    @DisplayName("InsufficientPrivilegesException should have 403 status")
    void testInsufficientPrivilegesException_Http403() {
        // Arrange
        InsufficientPrivilegesException exception = new InsufficientPrivilegesException("DELETE_USER");

        // Assert
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(exception.getErrorCode()).isEqualTo("INSUFFICIENT_PRIVILEGES");
        assertThat(exception.getMessage()).contains("DELETE_USER");
    }

    @Test
    @DisplayName("InvalidStateTransitionException should have 422 status")
    void testInvalidStateTransitionException_Http422() {
        // Arrange
        InvalidStateTransitionException exception = new InvalidStateTransitionException("ACTIVE", "PENDING");

        // Assert
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(exception.getErrorCode()).isEqualTo("INVALID_STATE_TRANSITION");
        assertThat(exception.getMessage()).contains("ACTIVE");
    }

    @Test
    @DisplayName("All domain exceptions extend DomainException")
    void testExceptionHierarchy() {
        // Arrange & Act & Assert
        assertThat(new UserNotFoundException(1L)).isInstanceOf(DomainException.class);
        assertThat(new DuplicateEmailException("test@example.com")).isInstanceOf(DomainException.class);
        assertThat(new InsufficientPrivilegesException("ACTION")).isInstanceOf(DomainException.class);
        assertThat(new InvalidStateTransitionException("A", "B")).isInstanceOf(DomainException.class);
    }
}
