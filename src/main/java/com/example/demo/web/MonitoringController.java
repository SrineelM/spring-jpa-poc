package com.example.demo.web;

import com.example.demo.health.DatabaseHealthIndicator;
import com.example.demo.service.UserService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Monitoring and health check controller for operational visibility
 */
@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);
    
    private final DatabaseHealthIndicator databaseHealthIndicator;
    private final UserService userService;

    public MonitoringController(DatabaseHealthIndicator databaseHealthIndicator, UserService userService) {
        this.databaseHealthIndicator = databaseHealthIndicator;
        this.userService = userService;
    }

    @GetMapping("/health")
    @Timed(value = "monitoring.health.duration", description = "Time taken for health check")
    public ResponseEntity<Map<String, Object>> getDetailedHealth() {
        logger.debug("Performing detailed health check");
        
        Map<String, Object> health = new HashMap<>();
        
        // Application health
        health.put("status", "UP");
        health.put("timestamp", Instant.now().toString());
        
        // Database health
        boolean dbHealthy = databaseHealthIndicator.isHealthy();
        Map<String, Object> database = new HashMap<>();
        database.put("status", dbHealthy ? "UP" : "DOWN");
        database.put("details", dbHealthy ? "Database connection successful" : "Database connection failed");
        health.put("database", database);
        
        // JVM metrics
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("uptime", runtimeBean.getUptime());
        jvm.put("maxMemory", memoryBean.getHeapMemoryUsage().getMax());
        jvm.put("usedMemory", memoryBean.getHeapMemoryUsage().getUsed());
        jvm.put("freeMemory", memoryBean.getHeapMemoryUsage().getMax() - memoryBean.getHeapMemoryUsage().getUsed());
        health.put("jvm", jvm);
        
        // Application metrics
        try {
            Map<String, Object> appMetrics = new HashMap<>();
            appMetrics.put("totalUsers", userService.getTotalUserCount());
            health.put("application", appMetrics);
        } catch (Exception e) {
            logger.warn("Failed to collect application metrics", e);
            health.put("application", Map.of("status", "ERROR", "message", "Failed to collect metrics"));
        }
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    @Timed(value = "monitoring.metrics.duration", description = "Time taken to collect metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        logger.debug("Collecting application metrics");
        
        Map<String, Object> metrics = new HashMap<>();
        
        // System metrics
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> system = new HashMap<>();
        system.put("processors", runtime.availableProcessors());
        system.put("totalMemory", runtime.totalMemory());
        system.put("freeMemory", runtime.freeMemory());
        system.put("maxMemory", runtime.maxMemory());
        metrics.put("system", system);
        
        // Application metrics
        Map<String, Object> application = new HashMap<>();
        try {
            application.put("userCount", userService.getTotalUserCount());
            application.put("databaseStatus", databaseHealthIndicator.getHealthStatus());
        } catch (Exception e) {
            logger.warn("Error collecting application metrics", e);
            application.put("error", "Failed to collect some metrics");
        }
        metrics.put("application", application);
        
        metrics.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/readiness")
    public ResponseEntity<Map<String, String>> getReadiness() {
        Map<String, String> readiness = new HashMap<>();
        
        boolean ready = databaseHealthIndicator.isHealthy();
        
        readiness.put("status", ready ? "READY" : "NOT_READY");
        readiness.put("database", databaseHealthIndicator.getHealthStatus());
        readiness.put("timestamp", Instant.now().toString());
        
        return ready ? ResponseEntity.ok(readiness) : ResponseEntity.status(503).body(readiness);
    }

    @GetMapping("/liveness")
    public ResponseEntity<Map<String, String>> getLiveness() {
        Map<String, String> liveness = new HashMap<>();
        liveness.put("status", "ALIVE");
        liveness.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.ok(liveness);
    }
}
