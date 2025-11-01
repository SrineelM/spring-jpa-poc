package com.example.demo.event;

import com.example.demo.repository.FailedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled service for retrying failed events from the Dead Letter Queue.
 * Runs periodically to attempt processing of captured failed events.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.event-retry.enabled", havingValue = "true", matchIfMissing = true)
public class FailedEventRetryScheduler {

    private final FailedEventRepository failedEventRepository;

    public FailedEventRetryScheduler(
            FailedEventRepository failedEventRepository,
            ApplicationEventPublisher eventPublisher,
            ObjectMapper objectMapper) {
        this.failedEventRepository = failedEventRepository;
    }

    /**
     * Scheduled task to retry failed events.
     * Runs every 30 seconds (configurable via property).
     */
    @Scheduled(fixedRateString = "${app.event-retry.interval:30000}")
    @Transactional
    public void retryFailedEvents() {
        try {
            // Find events ready for retry
            List<FailedEventRecord> readyForRetry = failedEventRepository.findReadyForRetry(LocalDateTime.now());

            if (readyForRetry.isEmpty()) {
                return;
            }

            log.info("Attempting to retry {} failed events", readyForRetry.size());

            for (FailedEventRecord failedEvent : readyForRetry) {
                retryEvent(failedEvent);
            }

            // Find events that exceeded max retries and mark as abandoned
            List<FailedEventRecord> exceededMaxRetries = failedEventRepository.findExceededMaxRetries();
            for (FailedEventRecord failedEvent : exceededMaxRetries) {
                failedEvent.markAbandoned();
                failedEventRepository.save(failedEvent);
                log.warn(
                        "Event abandoned after {} retry attempts (ID: {}): {}",
                        failedEvent.getMaxRetries(),
                        failedEvent.getId(),
                        failedEvent.getEventType());
            }

        } catch (Exception e) {
            log.error("Error in retry scheduler", e);
        }
    }

    /**
     * Attempt to retry a single failed event.
     */
    private void retryEvent(FailedEventRecord failedEvent) {
        try {
            log.debug(
                    "Retrying failed event (ID: {}, Attempt: {}/{}): {}",
                    failedEvent.getId(),
                    failedEvent.getRetryCount() + 1,
                    failedEvent.getMaxRetries(),
                    failedEvent.getEventType());

            // Reconstruct event from payload (simplified - in production, use proper deserialization)
            // Note: You would typically have a registry of event types to reconstruct the actual event
            // For now, we log and increment the retry count

            failedEvent.recordRetryAttempt("Retry attempt scheduled", "");

            if (failedEvent.canRetry()) {
                // Schedule next retry (exponential backoff: 2^retryCount minutes)
                int delaySeconds = (int) Math.pow(2, failedEvent.getRetryCount()) * 60;
                failedEvent.scheduleRetry(Math.min(delaySeconds, 3600)); // Max 1 hour
                log.info("Event rescheduled for retry (ID: {}) in {} seconds", failedEvent.getId(), delaySeconds);
            } else {
                failedEvent.markAbandoned();
                log.warn(
                        "Event abandoned - max retries exceeded (ID: {}): {}",
                        failedEvent.getId(),
                        failedEvent.getEventType());
            }

            failedEventRepository.save(failedEvent);

        } catch (Exception e) {
            log.error("Error retrying failed event (ID: {})", failedEvent.getId(), e);
            failedEvent.recordRetryAttempt(e.getMessage(), getStackTrace(e));
            failedEventRepository.save(failedEvent);
        }
    }

    /**
     * Get stack trace as string for error logging.
     */
    private String getStackTrace(Exception exception) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Manual retry of a specific failed event.
     */
    @Transactional
    public void manualRetry(Long failedEventId) {
        if (failedEventId == null) {
            log.warn("Failed event ID is null");
            return;
        }
        failedEventRepository
                .findById(failedEventId)
                .ifPresentOrElse(this::retryEvent, () -> log.warn("Failed event not found (ID: {})", failedEventId));
    }

    /**
     * Mark a failed event as resolved manually.
     */
    @Transactional
    public void markResolved(Long failedEventId, String resolution) {
        if (failedEventId == null) {
            log.warn("Failed event ID is null");
            return;
        }
        failedEventRepository
                .findById(failedEventId)
                .ifPresentOrElse(
                        failedEvent -> {
                            failedEvent.markResolved(resolution);
                            failedEventRepository.save(failedEvent);
                            log.info("Failed event marked as resolved (ID: {})", failedEventId);
                        },
                        () -> log.warn("Failed event not found (ID: {})", failedEventId));
    }
}
