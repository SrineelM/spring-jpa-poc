package com.example.demo.event;

import com.example.demo.domain.User;
import org.springframework.context.ApplicationEvent;

/**
 * Event published after the full registration workflow succeeds (which might
 * include validation, persistence, assigning default roles, etc.). Using a
 * separate event from simple creation allows for different semantics if needed.
 */
public class UserRegisteredEvent extends ApplicationEvent {

    private final User user; // Registered user snapshot

    public UserRegisteredEvent(Object source, User user) {
        super(source); // capture publisher for traceability
        this.user = user;
    }

    public User getUser() { return user; }
}
