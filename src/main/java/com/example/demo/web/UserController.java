package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.dto.UserSummaryDto;
import com.example.demo.health.DatabaseHealthIndicator;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/** Enhanced user controller with comprehensive validation, monitoring, and security */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;
  private final UserRepository userRepository;
  private final DatabaseHealthIndicator healthIndicator;

  public UserController(
      UserService userService,
      UserRepository userRepository,
      DatabaseHealthIndicator healthIndicator) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.healthIndicator = healthIndicator;
  }

  /** Enhanced search endpoint with validation and monitoring */
  @GetMapping("/search")
  @Timed(value = "user.search.duration", description = "Time taken to search users")
  @Counted(value = "user.search.requests", description = "Number of user search requests")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<Page<User>> searchUsers(
      @RequestParam(required = false) String name,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String role,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "asc") String sortDir) {

    logger.info(
        "User search request - name: {}, email: {}, role: {}, page: {}, size: {}",
        name,
        email,
        role,
        page,
        size);

    // Input validation
    if (page < 0) page = 0;
    if (size < 1) size = 20;
    if (size > 100) size = 100;
    if (!sortBy.matches("^(id|name|email|createdDate)$")) sortBy = "id";
    if (!sortDir.matches("^(asc|desc)$")) sortDir = "asc";

    Sort.Direction direction =
        sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

    Page<User> users = userService.searchUsers(name, email, role, pageable);
    return ResponseEntity.ok(users);
  }

  /** Get user summaries with caching */
  @GetMapping("/summaries")
  @Timed(value = "user.summaries.duration", description = "Time taken to get user summaries")
  @Counted(value = "user.summaries.requests", description = "Number of user summary requests")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<List<UserSummaryDto>> getUserSummaries() {
    logger.info("Fetching user summaries");
    List<UserSummaryDto> summaries = userRepository.findAllUserSummaries();
    return ResponseEntity.ok(summaries);
  }

  /** Get user by ID */
  @GetMapping("/{id}")
  @Timed(value = "user.findById.duration", description = "Time taken to find user by ID")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<User> getUserById(@PathVariable Long id) {
    logger.info("Fetching user with ID: {}", id);
    return userService
        .findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /** Get user statistics (admin only) */
  @GetMapping("/stats")
  @Timed(value = "user.stats.duration", description = "Time taken to get user stats")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> getUserStats() {
    logger.info("Fetching user statistics");

    Map<String, Object> stats = new HashMap<>();
    stats.put("totalUsers", userService.getTotalUserCount());
    stats.put("databaseHealth", healthIndicator.getHealthStatus());
    stats.put("timestamp", System.currentTimeMillis());

    return ResponseEntity.ok(stats);
  }

  /** Health check endpoint */
  @GetMapping("/health")
  public ResponseEntity<Map<String, String>> getHealth() {
    Map<String, String> health = new HashMap<>();
    health.put("status", "UP");
    health.put("database", healthIndicator.getHealthStatus());
    health.put("timestamp", String.valueOf(System.currentTimeMillis()));

    return ResponseEntity.ok(health);
  }
}
