package com.example.demo.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dead Letter Queue (DLQ) statistics DTO.
 * Provides overview of failed events by status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DLQStatistics {

    /**
     * Number of events pending initial processing.
     */
    private Long pending;

    /**
     * Number of events scheduled for retry but waiting.
     */
    private Long pendingRetry;

    /**
     * Number of events successfully resolved after retry.
     */
    private Long resolved;

    /**
     * Number of events abandoned after exceeding max retries.
     */
    private Long abandoned;

    /**
     * Total number of events in DLQ.
     */
    private Long total;

    /**
     * Get health status based on DLQ metrics.
     */
    public String getHealthStatus() {
        if (pending == null || pending == 0) {
            return "HEALTHY";
        }

        if (pending > 100) {
            return "CRITICAL";
        } else if (pending > 50) {
            return "WARNING";
        } else {
            return "DEGRADED";
        }
    }

    /**
     * Get recovery rate (resolved / (resolved + abandoned)).
     */
    public Double getRecoveryRate() {
        if (resolved == null || abandoned == null) {
            return 0.0;
        }

        long processed = resolved + abandoned;
        if (processed == 0) {
            return 0.0;
        }

        return (double) resolved / processed * 100;
    }
}
