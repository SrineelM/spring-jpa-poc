package com.example.demo.repository;

import com.example.demo.event.FailedEventRecord;
import com.example.demo.event.FailedEventStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing failed event records (Dead Letter Queue).
 */
@Repository
public interface FailedEventRepository extends JpaRepository<FailedEventRecord, Long> {

    /**
     * Find all failed events pending retry.
     */
    List<FailedEventRecord> findByStatusOrderByCreatedAtAsc(FailedEventStatus status);

    /**
     * Find failed events ready for retry (status is PENDING_RETRY and nextRetryTime has passed).
     */
    @Query("SELECT fer FROM FailedEventRecord fer " + "WHERE fer.status = 'PENDING_RETRY' "
            + "AND fer.nextRetryTime <= :now "
            + "ORDER BY fer.nextRetryTime ASC")
    List<FailedEventRecord> findReadyForRetry(@Param("now") LocalDateTime now);

    /**
     * Find failed events that have exceeded max retries.
     */
    @Query("SELECT fer FROM FailedEventRecord fer " + "WHERE fer.retryCount >= fer.maxRetries "
            + "AND fer.status != 'RESOLVED' "
            + "ORDER BY fer.updatedAt ASC")
    List<FailedEventRecord> findExceededMaxRetries();

    /**
     * Find failed events by status and event type.
     */
    List<FailedEventRecord> findByStatusAndEventTypeOrderByCreatedAtDesc(FailedEventStatus status, String eventType);

    /**
     * Count failed events by status.
     */
    Long countByStatus(FailedEventStatus status);

    /**
     * Find resolved events from a specific time period.
     */
    @Query("SELECT fer FROM FailedEventRecord fer " + "WHERE fer.status = 'RESOLVED' "
            + "AND fer.resolvedTime >= :startTime "
            + "AND fer.resolvedTime <= :endTime "
            + "ORDER BY fer.resolvedTime DESC")
    List<FailedEventRecord> findResolvedInPeriod(
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
