package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * An abstract base class for all domain entities.
 *
 * <p>The {@link MappedSuperclass} annotation indicates that this class provides persistent
 * properties for its subclasses, but it is not an entity itself and is not mapped to a database
 * table. This promotes code reuse and a consistent structure for all entities.
 *
 * <p>The {@link EntityListeners} annotation with {@link AuditingEntityListener} enables automatic
 * population of the auditing fields (createdBy, createdDate, lastModifiedBy, lastModifiedDate).
 * This requires an `AuditorAware` bean to be configured in the application context.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  /**
   * The primary key for the entity. The {@link GeneratedValue} annotation with {@code strategy =
   * GenerationType.IDENTITY} delegates the generation of the ID to the database, which is efficient
   * and common for relational databases.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * The version field for optimistic locking. JPA uses this field to detect concurrent
   * modifications to the same entity, preventing lost updates. The value is automatically
   * incremented by the persistence provider on each update. This is a crucial mechanism for
   * maintaining data integrity in concurrent environments.
   */
  @Version private Long version;

  /**
   * The timestamp when the entity was first created. This field is automatically populated by the
   * {@link AuditingEntityListener} on persist. It is marked as non-nullable and not updatable to
   * ensure it's a permanent record of creation time.
   */
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private Instant createdDate;

  /**
   * The timestamp when the entity was last modified. This field is automatically updated by the
   * {@link AuditingEntityListener} on update.
   */
  @LastModifiedDate private Instant lastModifiedDate;

  /**
   * The user who created the entity. This field is automatically populated by the {@link
   * AuditingEntityListener} from the configured `AuditorAware` bean. It is marked as not updatable
   * to preserve the original creator.
   */
  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  /**
   * The user who last modified the entity. This field is automatically updated by the {@link
   * AuditingEntityListener} from the configured `AuditorAware` bean.
   */
  @LastModifiedBy private String lastModifiedBy;

  // --- Getters ---

  public Long getId() {
    return id;
  }

  public Long getVersion() {
    return version;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public Instant getLastModifiedDate() {
    return lastModifiedDate;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }
}
