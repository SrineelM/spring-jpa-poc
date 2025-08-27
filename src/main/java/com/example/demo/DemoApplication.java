package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Enhanced Spring Boot application with comprehensive enterprise features: - JPA Auditing for
 * entity tracking - Caching for performance optimization - Async processing for non-blocking
 * operations - Transaction management - Resilience patterns (Circuit Breaker, Retry, Rate Limiting,
 * etc.) - Security with JWT authentication - Observability with distributed tracing and metrics -
 * Structured logging with request correlation
 */
@SpringBootApplication // Enables auto-configuration, component scanning, and configuration
// properties
@EnableJpaAuditing // Enables JPA auditing to automatically populate @CreatedDate,
// @LastModifiedDate, etc.
@EnableCaching // Enables Spring's caching abstraction, allowing methods to be cached using
// annotations like @Cacheable
@EnableAsync // Enables asynchronous method execution using @Async annotation
@EnableTransactionManagement // Enables Spring's declarative transaction management
public class DemoApplication {

    // Main entry point for the Spring Boot application
    // This method bootstraps the Spring application, sets up the Spring context,
    // and starts the embedded server (if applicable) using auto-configuration.
    public static void main(String[] args) {
        // Set system properties for enhanced observability
        // This sets the application name for metrics and tracing
        System.setProperty("spring.application.name", "spring-jpa-poc");
        // This sets the tracing sampling probability to 100% for development (adjust in production)
        System.setProperty("management.tracing.sampling.probability", "1.0");

        // Create SpringApplication instance with this class as the main configuration
        SpringApplication app = new SpringApplication(DemoApplication.class);

        // Register a shutdown hook to ensure graceful shutdown of the application context
        app.setRegisterShutdownHook(true);

        // Start the Spring application by invoking SpringApplication.run
        // This initializes the application context, loads configurations, and performs classpath
        // scanning
        app.run(args);
        // After run returns, the application context is fully initialized
    }
}
