package com.example.demo.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced external API service with comprehensive resilience patterns and observability
 */
@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final AtomicInteger successCounter = new AtomicInteger(0);
    private final AtomicInteger failureCounter = new AtomicInteger(0);

    /**
     * Enhanced external API call with comprehensive resilience patterns and monitoring
     */
    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "fallback")
    @Retry(name = "myRetry")
    @TimeLimiter(name = "myTimeLimiter")
    @Bulkhead(name = "myBulkhead")
    @RateLimiter(name = "myRateLimiter")
    @Timed(value = "external.api.call.duration", description = "Time taken for external API calls")
    @Counted(value = "external.api.call.requests", description = "Number of external API calls")
    public CompletableFuture<String> fetchData() {
        return CompletableFuture.supplyAsync(() -> {
            int requestId = requestCounter.incrementAndGet();
            String correlationId = MDC.get("requestId");
            
            try {
                logger.info("Starting external API call #{} with correlation ID: {}", requestId, correlationId);
                
                // Simulate network latency
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 500)); // variable simulated latency
                
                // Simulate random failures (30% failure rate for demonstration)
                if (ThreadLocalRandom.current().nextDouble() < 0.3) { // inject failure for resilience demonstration
                    failureCounter.incrementAndGet();
                    logger.warn("External API call #{} failed - simulated failure", requestId);
                    throw new RuntimeException("Simulated external service failure");
                }
                
                successCounter.incrementAndGet();
                String response = "External API Response #" + requestId + " at " + System.currentTimeMillis(); // synthetic payload
                
                logger.info("External API call #{} completed successfully. Response: {}", requestId, response);
                // (Metrics) Tag outcome=success manually via custom Counter/Timer if finer-grained labels needed.
                return response;
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                failureCounter.incrementAndGet();
                logger.error("External API call #{} interrupted", requestId, e);
                throw new RuntimeException("External API call interrupted", e);
                
            } catch (Exception e) {
                failureCounter.incrementAndGet();
                logger.error("External API call #{} failed with error", requestId, e);
                throw e;
            }
        });
    }

    /**
     * Enhanced fallback method with contextual information
     */
    public CompletableFuture<String> fallback(Throwable t) {
        String correlationId = MDC.get("requestId");
        String fallbackResponse = String.format(
            "Fallback response (correlation: %s, timestamp: %d) - External service unavailable: %s", 
            correlationId, System.currentTimeMillis(), t.getMessage()
        );
        
        logger.error("Fallback executed for external API call. Correlation ID: {}, Error: {}", 
                correlationId, t.getMessage());
        
        return CompletableFuture.completedFuture(fallbackResponse);
    }

    /**
     * Enhanced health check for external service
     */
    @TimeLimiter(name = "myTimeLimiter")
    @Timed(value = "external.api.health.duration", description = "Time taken for external API health check")
    public CompletableFuture<Boolean> isExternalServiceHealthy() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Performing external service health check");
                
                // Simulate health check (80% success rate)
                boolean healthy = ThreadLocalRandom.current().nextDouble() < 0.8;
                
                logger.debug("External service health check result: {}", healthy ? "HEALTHY" : "UNHEALTHY");
                return healthy;
                
            } catch (Exception e) {
                logger.error("External service health check failed", e);
                return false;
            }
        });
    }

    /**
     * Get service statistics for monitoring
     */
    public ServiceStats getServiceStats() {
        return new ServiceStats(
            requestCounter.get(),
            successCounter.get(),
            failureCounter.get(),
            calculateSuccessRate()
        );
    }

    private double calculateSuccessRate() {
        int total = requestCounter.get();
        if (total == 0) return 0.0;
        return (double) successCounter.get() / total * 100.0;
    }
    // (Observation) For production, expose these counters as Gauges/FunctionCounters or restructure using MeterRegistry injection.

    /**
     * Service statistics DTO
     */
    public static class ServiceStats {
        private final int totalRequests;
        private final int successfulRequests;
        private final int failedRequests;
        private final double successRate;

        public ServiceStats(int totalRequests, int successfulRequests, int failedRequests, double successRate) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.successRate = successRate;
        }

        public int getTotalRequests() { return totalRequests; }
        public int getSuccessfulRequests() { return successfulRequests; }
        public int getFailedRequests() { return failedRequests; }
        public double getSuccessRate() { return successRate; }
    }
}
