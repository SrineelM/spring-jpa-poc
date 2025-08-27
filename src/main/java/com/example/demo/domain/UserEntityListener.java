package com.example.demo.domain;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An entity listener specifically for the User entity. This listener hooks into JPA lifecycle
 * events for User entities to perform actions like logging or sending notifications after a user is
 * created or updated.
 */
public class UserEntityListener {

    private static final Logger log = LoggerFactory.getLogger(UserEntityListener.class);

    /**
     * This method is executed after a new User entity is successfully saved to the database. The
     * {@link PostPersist} annotation marks it as a JPA lifecycle callback.
     *
     * @param user The User entity that was just persisted.
     */
    @PostPersist
    public void afterSave(User user) {
        log.info("[EntityListener] A new user has been created with email: {}", user.getEmail());
        // In a real application, you might publish an event here, e.g., for sending a welcome email.
    }

    /**
     * This method is executed after a User entity is successfully updated in the database. The {@link
     * PostUpdate} annotation marks it as a JPA lifecycle callback.
     *
     * @param user The User entity that was just updated.
     */
    @PostUpdate
    public void afterUpdate(User user) {
        log.info("[EntityListener] User with email [{}] has been updated.", user.getEmail());
    }
}
