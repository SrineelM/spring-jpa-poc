package com.example.demo.event;

/**
 * Status enum for failed event records in the Dead Letter Queue.
 */
public enum FailedEventStatus {
    /**
     * Event has failed and is pending retry.
     */
    PENDING,

    /**
     * Event is scheduled for retry but waiting.
     */
    PENDING_RETRY,

    /**
     * Event has been successfully processed after retry.
     */
    RESOLVED,

    /**
     * Event has been abandoned after exceeding max retries.
     */
    ABANDONED
}
