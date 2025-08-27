package com.example.demo.event;

import com.example.demo.domain.User;
import org.springframework.context.ApplicationEvent;

/**
 * Synchronous simple variant of a user lifecycle event. Published right after a
 * User entity is persisted (creation use case) to allow other parts of the
 * application to react (logging, analytics, provisioning, etc.) without
 * tightly coupling them to the creation logic.
 */
public class UserCreatedEvent extends ApplicationEvent {
    /** The newly created user aggregate root. */
    private final User user;

    /**
     * @param source publisher (commonly the service issuing the event)
     * @param user domain object representing the persisted user state
     */
    public UserCreatedEvent(Object source, User user) {
        super(source); // pass publisher to base event (useful for debugging / tracing)
        this.user = user;
    }

    /** Expose immutable user reference to listeners. */
    public User getUser() { return user; }
}
