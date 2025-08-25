package com.example.demo.event;

import com.example.demo.domain.User;
import org.springframework.context.ApplicationEvent;

/**
 * This event is published when a new user is successfully registered.
 * It holds the newly created User object, which can be used by listeners
 * to perform actions like sending a welcome email or creating a user profile.
 */
public class UserRegisteredEvent extends ApplicationEvent {

    private final User user;

    /**
     * Create a new UserRegisteredEvent.
     *
     * @param source The object on which the event initially occurred (never {@code null}).
     * @param user   The user that was registered.
     */
    public UserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
