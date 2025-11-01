package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Unit tests for UserService business logic.
 * Tests exception handling, validation, and core service methods.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");
        testUser.setPassword("hashed_password");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void testFindById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty Optional when user not found by ID")
    void testFindById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when finding by ID with throw")
    void testFindByIdOrThrow_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.findByIdOrThrow(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User with ID 999 not found");
    }

    @Test
    @DisplayName("Should save new user successfully")
    void testSave_NewUser_Success() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setName("New User");
        newUser.setPassword("password");
        newUser.setRole(Role.USER);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(testUser);

        // Act
        User result = userService.save(newUser);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findByEmail("newuser@example.com");
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    @DisplayName("Should throw DuplicateEmailException when email already exists")
    void testSave_DuplicateEmail() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("existing@example.com");
        newUser.setName("Another User");

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.save(newUser))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email 'existing@example.com' is already registered");

        verify(userRepository, never()).save(newUser);
    }

    @Test
    @DisplayName("Should update existing user with new email successfully")
    void testSave_UpdateUser_NewEmailSuccess() {
        // Arrange
        User updateUser = new User();
        updateUser.setEmail("newemail@example.com");
        updateUser.setName("Updated User");

        when(userRepository.findByEmail("newemail@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(updateUser)).thenReturn(updateUser);

        // Act
        User result = userService.save(updateUser);

        // Assert
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
        verify(userRepository, times(1)).findByEmail("newemail@example.com");
        verify(userRepository, times(1)).save(updateUser);
    }

    @Test
    @DisplayName("Should get total user count successfully")
    void testGetTotalUserCount() {
        // Arrange
        when(userRepository.count()).thenReturn(5L);

        // Act
        long count = userService.getTotalUserCount();

        // Assert
        assertThat(count).isEqualTo(5L);
        verify(userRepository, times(1)).count();
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteById_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteById(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void testDeleteById_NotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteById(999L)).isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).deleteById(999L);
    }

    @Test
    @DisplayName("Should search users with pagination successfully")
    @SuppressWarnings({"unchecked", "null"})
    void testSearchUsers_WithPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(java.util.List.of(testUser), pageable, 1);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<User> result = userService.searchUsers(null, null, null, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(userRepository, times(1))
                .findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }
}
