package com.example.demo.event;

import com.example.demo.repository.FailedEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resilient Event Publisher with Dead Letter Queue (DLQ) support.
 * Publishes events with automatic capture of failures for later retry.
 */
@Service
@Slf4j
public class ResilientEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final FailedEventRepository failedEventRepository;
    private final ObjectMapper objectMapper;

    public ResilientEventPublisher(
            ApplicationEventPublisher eventPublisher,
            FailedEventRepository failedEventRepository,
            ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.failedEventRepository = failedEventRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish an event with automatic failure handling.
     * If the event handler fails, the event is captured in the DLQ.
     *
     * @param event the domain event to publish
     */
    @Transactional
    public void publishEvent(Object event) {
        try {
            log.debug("Publishing event: {}", event.getClass().getSimpleName());
            eventPublisher.publishEvent(event);
            log.info("Event published successfully: {}", event.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("Failed to publish event: {}", event.getClass().getSimpleName(), e);
            captureFailedEvent(event, e);
        }
    }

    /**
     * Capture a failed event in the Dead Letter Queue for later retry.
     */
    @SuppressWarnings("null")
    private void captureFailedEvent(Object event, Exception exception) {
        try {
            String eventType = event.getClass().getSimpleName();
            String eventPayload = objectMapper.writeValueAsString(event);
            String errorMessage = exception.getMessage();
            String stackTrace = getStackTrace(exception);

            FailedEventRecord failedEvent = FailedEventRecord.builder()
                    .eventType(eventType)
                    .eventPayload(eventPayload)
                    .errorMessage(errorMessage)
                    .stackTrace(stackTrace)
                    .retryCount(0)
                    .maxRetries(3)
                    .status(FailedEventStatus.PENDING)
                    .build();

            FailedEventRecord saved = failedEventRepository.save(failedEvent);
            log.warn("Failed event captured in DLQ (ID: {}): {} - {}", saved.getId(), eventType, errorMessage);

        } catch (Exception captureError) {
            log.error("Failed to capture event in DLQ", captureError);
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
     * Get DLQ statistics.
     */
    public DLQStatistics getStatistics() {
        long pendingCount = failedEventRepository.countByStatus(FailedEventStatus.PENDING);
        long pendingRetryCount = failedEventRepository.countByStatus(FailedEventStatus.PENDING_RETRY);
        long resolvedCount = failedEventRepository.countByStatus(FailedEventStatus.RESOLVED);
        long abandonedCount = failedEventRepository.countByStatus(FailedEventStatus.ABANDONED);

        return DLQStatistics.builder()
                .pending(pendingCount)
                .pendingRetry(pendingRetryCount)
                .resolved(resolvedCount)
                .abandoned(abandonedCount)
                .total(pendingCount + pendingRetryCount + resolvedCount + abandonedCount)
                .build();
    }
}
