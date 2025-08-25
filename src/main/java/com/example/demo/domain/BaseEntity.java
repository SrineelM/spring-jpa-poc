package com.example.demo.domain;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * An abstract base class for all domain entities.
 * <p>
 * The {@link MappedSuperclass} annotation indicates that this class provides persistent properties
 * for its subclasses, but it is not an entity itself and is not mapped to a database table.
 * <p>
 * The {@link EntityListeners} annotation with {@link AuditingEntityListener} enables automatic
 * population of the auditing fields (createdBy, createdDate, lastModifiedBy, lastModifiedDate).
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * The primary key for the entity.
     * The {@link GeneratedValue} annotation with {@code strategy = GenerationType.IDENTITY}
     * indicates that the database is responsible for generating the primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The version field for optimistic locking.
     * JPA uses this field to detect concurrent modifications to the same entity,
     * preventing lost updates. The value is automatically incremented on each update.
     */
    @Version
    private Long version;

    /**
     * The timestamp when the entity was first created.
     * This field is automatically populated by the {@link AuditingEntityListener}.
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;

    /**
     * The timestamp when the entity was last modified.
     * This field is automatically updated by the {@link AuditingEntityListener}.
     */
    @LastModifiedDate
    private Instant lastModifiedDate;

    /**
     * The user who created the entity.
     * This field is automatically populated by the {@link AuditingEntityListener}.
     * Requires a {@code AuditorAware} bean to be configured.
     */
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    /**
     * The user who last modified the entity.
     * This field is automatically updated by the {@link AuditingEntityListener}.
     * Requires a {@code AuditorAware} bean to be configured.
     */
    @LastModifiedBy
    private String lastModifiedBy;

    // --- Getters ---

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public Instant getCreatedDate() { return createdDate; }
    public Instant getLastModifiedDate() { return lastModifiedDate; }
    public String getCreatedBy() { return createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
}
