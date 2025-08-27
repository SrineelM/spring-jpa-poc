package com.example.demo.web;

import com.example.demo.service.ExternalApiService;
import io.micrometer.core.annotation.Timed;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Enhanced resilience testing controller with comprehensive endpoints for testing all resilience
 * patterns
 */
@RestController
@RequestMapping("/api/resilience")
public class ResilienceController {

    private static final Logger logger = LoggerFactory.getLogger(ResilienceController.class);

    private final ExternalApiService externalApiService;

    public ResilienceController(ExternalApiService externalApiService) {
        this.externalApiService = externalApiService;
    }

    /** Test circuit breaker, retry, and other resilience patterns */
    @GetMapping("/fetch")
    @Timed(value = "resilience.fetch.duration", description = "Time taken for resilience fetch test")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> fetchData() {
        logger.info("Resilience test - fetching data from external service");

        long startTime = System.currentTimeMillis();

        return externalApiService
                .fetchData()
                .thenApply(data -> {
                    long duration = System.currentTimeMillis() - startTime;

                    Map<String, Object> response = new HashMap<>();
                    response.put("data", data);
                    response.put("duration", duration);
                    response.put("timestamp", System.currentTimeMillis());
                    response.put("status", "SUCCESS");

                    logger.info("Resilience test completed successfully in {}ms", duration);
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    long duration = System.currentTimeMillis() - startTime;

                    Map<String, Object> response = new HashMap<>();
                    response.put("error", throwable.getMessage());
                    response.put("duration", duration);
                    response.put("timestamp", System.currentTimeMillis());
                    response.put("status", "ERROR");

                    logger.error("Resilience test failed after {}ms", duration, throwable);
                    return ResponseEntity.status(500).body(response);
                });
    }

    /** Test multiple concurrent requests to trigger bulkhead */
    @PostMapping("/load-test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> loadTest(@RequestParam(defaultValue = "10") int requests) {
        logger.info("Starting load test with {} concurrent requests", requests);

        Map<String, Object> response = new HashMap<>();
        response.put("requestCount", requests);
        response.put("message", "Load test initiated");
        response.put("timestamp", System.currentTimeMillis());

        // Trigger multiple concurrent requests
        for (int i = 0; i < requests; i++) {
            externalApiService
                    .fetchData()
                    .thenAccept(data -> logger.debug("Load test request completed: {}", data))
                    .exceptionally(throwable -> {
                        logger.debug("Load test request failed: {}", throwable.getMessage());
                        return null;
                    });
        }

        return ResponseEntity.ok(response);
    }

    /** Get service health and statistics */
    @GetMapping("/health")
    @Timed(value = "resilience.health.duration", description = "Time taken for resilience health check")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getHealthStatus() {
        logger.debug("Checking external service health");

        return externalApiService
                .isExternalServiceHealthy()
                .thenApply(healthy -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put("externalServiceHealthy", healthy);
                    health.put("serviceStats", externalApiService.getServiceStats());
                    health.put("timestamp", System.currentTimeMillis());

                    return ResponseEntity.ok(health);
                })
                .exceptionally(throwable -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put("externalServiceHealthy", false);
                    health.put("error", throwable.getMessage());
                    health.put("timestamp", System.currentTimeMillis());

                    return ResponseEntity.status(503).body(health);
                });
    }

    /** Get detailed resilience statistics */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getResilienceStats() {
        logger.debug("Collecting resilience statistics");

        Map<String, Object> stats = new HashMap<>();

        // Service statistics
        ExternalApiService.ServiceStats serviceStats = externalApiService.getServiceStats();
        stats.put(
                "serviceStats",
                Map.of(
                        "totalRequests", serviceStats.getTotalRequests(),
                        "successfulRequests", serviceStats.getSuccessfulRequests(),
                        "failedRequests", serviceStats.getFailedRequests(),
                        "successRate", String.format("%.2f%%", serviceStats.getSuccessRate())));

        stats.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(stats);
    }

    /** Reset service statistics (admin only) */
    @PostMapping("/reset-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetStats() {
        logger.info("Resetting resilience statistics");

        // Note: In a real implementation, you'd have methods to reset counters
        Map<String, String> response = new HashMap<>();
        response.put("message", "Statistics reset (not implemented in demo)");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(response);
    }

    /** Test endpoint for demonstrating different resilience patterns */
    @GetMapping("/pattern/{pattern}")
    public ResponseEntity<Map<String, Object>> testPattern(@PathVariable String pattern) {
        logger.info("Testing resilience pattern: {}", pattern);

        Map<String, Object> response = new HashMap<>();
        response.put("pattern", pattern);
        response.put("timestamp", System.currentTimeMillis());

        switch (pattern.toLowerCase()) {
            case "circuitbreaker":
                response.put("description", "Circuit Breaker prevents cascading failures");
                response.put("behavior", "Opens after failure threshold, allows fast-fail");
                break;
            case "retry":
                response.put("description", "Retry attempts failed operations");
                response.put("behavior", "Retries with exponential backoff");
                break;
            case "timelimiter":
                response.put("description", "Time Limiter prevents long-running operations");
                response.put("behavior", "Cancels operations exceeding timeout");
                break;
            case "bulkhead":
                response.put("description", "Bulkhead isolates critical resources");
                response.put("behavior", "Limits concurrent executions");
                break;
            case "ratelimiter":
                response.put("description", "Rate Limiter controls request rate");
                response.put("behavior", "Allows specific number of requests per time period");
                break;
            default:
                response.put("error", "Unknown pattern");
                return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}
