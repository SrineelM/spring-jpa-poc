package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Enhanced Spring Boot application with comprehensive enterprise features:
 * - JPA Auditing for entity tracking
 * - Caching for performance optimization
 * - Async processing for non-blocking operations
 * - Transaction management
 * - Resilience patterns (Circuit Breaker, Retry, Rate Limiting, etc.)
 * - Security with JWT authentication
 * - Observability with distributed tracing and metrics
 * - Structured logging with request correlation
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class DemoApplication {
    
    public static void main(String[] args) {
        // Set system properties for enhanced observability
        System.setProperty("spring.application.name", "spring-jpa-poc");
        System.setProperty("management.tracing.sampling.probability", "1.0");
        
        SpringApplication app = new SpringApplication(DemoApplication.class);
        
        // Add shutdown hook for graceful shutdown
        app.setRegisterShutdownHook(true);
        
        app.run(args);
    }
}
