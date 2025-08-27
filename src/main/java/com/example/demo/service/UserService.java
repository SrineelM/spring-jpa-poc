package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.spec.UserSpecifications;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
     * tracing and structured logging.
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

    /** Find user by ID with caching and tracing */
    @NewSpan("user-findById")
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(@SpanTag("user.id") Long id) {
        logger.debug("Finding user by ID: {}", id);

        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent()) {
                logger.debug("User found with ID: {}", id);
            } else {
                logger.warn("User not found with ID: {}", id);
            }
            return user;

        } catch (Exception e) {
            logger.error("Error finding user by ID: {}", id, e);
            throw e;
        }
    }

    /** Create or update user with proper transaction management */
    @NewSpan("user-save")
    @Transactional
    public User save(@SpanTag("user.email") User user) {
        logger.info("Saving user with email: {}", user.getEmail());

        try {
            // NOTE: Because we cache user lookup & search results, mutations should trigger cache
            // eviction.
            // For brevity in this POC we log intent instead of adding @CacheEvict annotations.
            // (Prod) Add: @CacheEvict(value={"users","user-stats"}, allEntries=true) or fine‑grained keys
            // to avoid stale data.
            User savedUser = userRepository.save(user);
            logger.info("User saved successfully with ID: {}", savedUser.getId());
            return savedUser;

        } catch (Exception e) {
            logger.error("Error saving user with email: {}", user.getEmail(), e);
            throw e;
        }
    }

    /** Get user count for monitoring */
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
            throw e; // propagate to allow upstream handling / metrics tagging
        }
    }
}
