package com.example.demo.domain;

import jakarta.persistence.PrePersist;
import java.time.Instant;

public class AuditEntityListener {

    @PrePersist
    public void beforeCreate(Object entity) {
        System.out.println("[AuditEntityListener] About to persist: " + entity.getClass().getSimpleName() + " at " + Instant.now());
    }
}
