package com.example.demo.event;

import com.example.demo.domain.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Failed Event Record for Dead Letter Queue (DLQ).
 * Captures events that failed processing for later retry or manual investigation.
 */
@Entity
@Table(name = "failed_events")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedEventRecord extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventPayload;

    @Column(nullable = false, length = 500)
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(nullable = false)
    @lombok.Builder.Default
    private Integer retryCount = 0;

    @Column(nullable = false)
    @lombok.Builder.Default
    private Integer maxRetries = 3;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private FailedEventStatus status = FailedEventStatus.PENDING;

    @Column
    private LocalDateTime nextRetryTime;

    @Column
    private LocalDateTime lastRetryTime;

    @Column
    private LocalDateTime resolvedTime;

    @Column(length = 500)
    private String resolution;

    /**
     * Check if this failed event can be retried.
     */
    public boolean canRetry() {
        return retryCount < maxRetries && status != FailedEventStatus.RESOLVED && status != FailedEventStatus.ABANDONED;
    }

    /**
     * Mark as ready for retry.
     */
    public void scheduleRetry(int delaySeconds) {
        this.nextRetryTime = LocalDateTime.now().plusSeconds(delaySeconds);
        this.status = FailedEventStatus.PENDING_RETRY;
    }

    /**
     * Mark as successfully resolved.
     */
    public void markResolved(String resolutionMessage) {
        this.status = FailedEventStatus.RESOLVED;
        this.resolvedTime = LocalDateTime.now();
        this.resolution = resolutionMessage;
    }

    /**
     * Mark as abandoned after max retries.
     */
    public void markAbandoned() {
        this.status = FailedEventStatus.ABANDONED;
        this.resolution = "Max retries (" + maxRetries + ") exceeded";
    }

    /**
     * Record a retry attempt.
     */
    public void recordRetryAttempt(String errorMsg, String trace) {
        this.retryCount++;
        this.lastRetryTime = LocalDateTime.now();
        this.errorMessage = errorMsg;
        this.stackTrace = trace;
    }
}
