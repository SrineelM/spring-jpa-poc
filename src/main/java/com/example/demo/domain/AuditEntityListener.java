package com.example.demo.domain;

import jakarta.persistence.PrePersist;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic entity listener that can be attached to any entity. This listener demonstrates how to
 * hook into JPA's lifecycle events. In this case, it logs a message before an entity is persisted.
 */
public class AuditEntityListener {

  private static final Logger log = LoggerFactory.getLogger(AuditEntityListener.class);

  /**
   * This method is executed before a new entity is saved to the database. It is marked with the
   * {@link PrePersist} annotation, which is a JPA lifecycle callback.
   *
   * @param entity The entity object that is about to be persisted.
   */
  @PrePersist
  public void beforeCreate(Object entity) {
    log.info(
        "[AuditEntityListener] About to persist a new {} at {}",
        entity.getClass().getSimpleName(),
        Instant.now());
  }
}
