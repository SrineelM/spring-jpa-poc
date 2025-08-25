package com.example.demo.event;

import com.example.demo.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * This class is a listener that handles application events.
 * It demonstrates how to create a decoupled, event-driven architecture.
 */
@Component
public class UserRegistrationListener {

    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationListener.class);

    /**
     * This method listens for the {@link UserRegisteredEvent}.
     * The {@link EventListener} annotation marks this method as an event handler.
     * The {@link Async} annotation makes the execution of this method asynchronous,
     * so it doesn't block the main thread that published the event (e.g., the user registration request).
     *
     * @param event The UserRegisteredEvent that was published.
     */
    @EventListener
    @Async
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        User user = event.getUser();
        logger.info("Asynchronously handling user registration event for user: {}", user.getEmail());

        // Simulate sending a welcome email, which might be a slow operation.
        try {
            Thread.sleep(2000); // Simulate a 2-second delay
            logger.info("Welcome email sent to {}", user.getEmail());
        } catch (InterruptedException e) {
            logger.error("Email sending was interrupted for user: {}", user.getEmail(), e);
            Thread.currentThread().interrupt();
        }

        // Other actions, like creating a default profile, could be performed here.
    }
}
