package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.exception.DuplicateEmailException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.spec.UserSpecifications;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Enhanced user service with logging, caching, and distributed tracing */
@Service
@Transactional(readOnly = true)
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Searches for users with dynamic filtering, pagination, and sorting. Enhanced with distributed
     * tracing, caching, and structured logging.
     */
    @NewSpan("user-search")
    @Cacheable(
            value = "users",
            key = "#name + '_' + #email + '_' + #role + '_' + #pageable.pageNumber + '_' +" + " #pageable.pageSize")
    public Page<User> searchUsers(
            @SpanTag("search.name") String name,
            @SpanTag("search.email") String email,
            @SpanTag("search.role") String role,
            Pageable pageable) {

        logger.info(
                "Searching users with filters - name: {}, email: {}, role: {}, page: {}, size: {}",
                name,
                email,
                role,
                pageable.getPageNumber(),
                pageable.getPageSize());

        try {
            // Start with a neutral Specification (always true) instead of deprecated where(null)
            Specification<User> spec = (root, query, cb) -> cb.conjunction();

            if (name != null && !name.trim().isEmpty()) { // dynamic spec building (null-safe)
                spec = spec.and(UserSpecifications.nameContains(name));
                logger.debug("Applied name filter: {}", name);
            }
            if (email != null && !email.trim().isEmpty()) {
                spec = spec.and(UserSpecifications.hasEmail(email));
                logger.debug("Applied email filter: {}", email);
            }
            if (role != null && !role.trim().isEmpty()) { // role passed as string; spec converts to enum internally
                spec = spec.and(UserSpecifications.roleIs(role));
                logger.debug("Applied role filter: {}", role);
            }

            Page<User> result = userRepository.findAll(spec, pageable);

            logger.info(
                    "User search completed successfully. Found {} users out of {} total",
                    result.getNumberOfElements(),
                    result.getTotalElements());

            return result;

        } catch (Exception e) {
            logger.error(
                    "Error occurred while searching users with filters - name: {}, email: {}, role: {}",
                    name,
                    email,
                    role,
                    e);
            throw e;
        }
    }

    /**
     * Find user by ID (optional). Results are cached.
     */
    @NewSpan("user-findById-optional")
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(@SpanTag("user.id") Long id) {
        logger.debug("Finding user by ID (optional): {}", id);

        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.debug("User found with ID: {}", id);
            } else {
                logger.debug("User not found with ID: {}", id);
            }
            return user;

        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Find user by ID or throw UserNotFoundException (404).
     * Results are cached.
     */
    @NewSpan("user-findById-or-throw")
    @Cacheable(value = "users", key = "#id")
    public User findByIdOrThrow(@SpanTag("user.id") Long id) {
        logger.debug("Finding user by ID (with throw): {}", id);

        try {
            return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));

        } catch (UserNotFoundException e) {
            logger.warn("User not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Create or update user with proper transaction management.
     * Throws DuplicateEmailException if email already exists.
     * Cache is fully invalidated on write to prevent stale data.
     */
    @NewSpan("user-save")
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public User save(@SpanTag("user.email") User user) {
        logger.info("Saving user with email: {}", user.getEmail());

        try {
            // Check for duplicate email (only if creating new or changing email)
            if (user.getId() == null) {
                // New user: check if email exists
                if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                    throw new DuplicateEmailException(user.getEmail());
                }
            } else {
                // Updating existing user: check if email changed and if new email exists
                Optional<User> existing = userRepository.findById(user.getId());
                if (existing.isPresent()) {
                    String currentEmail = existing.get().getEmail();
                    if (!currentEmail.equals(user.getEmail())
                            && userRepository.findByEmail(user.getEmail()).isPresent()) {
                        throw new DuplicateEmailException(user.getEmail());
                    }
                }
            }

            User savedUser = userRepository.save(user);
            logger.info("User saved successfully with ID: {}", savedUser.getId());
            return savedUser;

        } catch (DuplicateEmailException e) {
            logger.warn("Duplicate email during save: {}", user.getEmail());
            throw e;
        } catch (Exception e) {
            logger.error("Error saving user with email: {}", user.getEmail(), e);
            throw e;
        }
    }

    /**
     * Delete user by ID. Invalidates cache.
     * Throws UserNotFoundException if user doesn't exist.
     */
    @NewSpan("user-delete")
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void deleteById(@SpanTag("user.id") Long id) {
        logger.info("Deleting user with ID: {}", id);

        try {
            // Verify user exists before deleting
            User user = findByIdOrThrow(id);
            userRepository.deleteById(id);
            logger.info("User deleted successfully with ID: {}", id);

        } catch (UserNotFoundException e) {
            logger.warn("Cannot delete: user not found with ID: {}", id);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting user with ID: {}", id, e);
            throw e;
        }
    }

    /**
     * Get user count for monitoring. Cached for performance.
     */
    @NewSpan("user-count")
    @Cacheable(value = "user-stats", key = "'total-count'")
    public long getTotalUserCount() {
        logger.debug("Getting total user count");

        try {
            long count = userRepository.count();
            logger.debug("Total user count: {}", count);
            return count;

        } catch (Exception e) {
            logger.error("Error getting user count", e);
            throw e;
        }
    }
}
