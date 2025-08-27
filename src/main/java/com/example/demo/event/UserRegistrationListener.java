package com.example.demo.event;

import com.example.demo.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Asynchronous listener reacting to registration completion. Demonstrates how long running / side
 * effect work (emails, analytics) can be offloaded so the HTTP request thread returns quickly.
 */
@Component
public class UserRegistrationListener {

  private static final Logger logger = LoggerFactory.getLogger(UserRegistrationListener.class);

  @EventListener
  @Async // executed in task executor thread pool configured via @EnableAsync / AsyncConfig
  public void handleUserRegisteredEvent(UserRegisteredEvent event) {
    User user = event.getUser();
    logger.info("Asynchronously handling user registration event for user: {}", user.getEmail());

    // Simulate an external call (e.g., email service). Replace sleep with real integration.
    try {
      Thread.sleep(2000); // Illustrative delay to show non-blocking of publisher thread
      logger.info("Welcome email sent to {}", user.getEmail());
    } catch (InterruptedException e) {
      logger.error("Email sending was interrupted for user: {}", user.getEmail(), e);
      Thread.currentThread().interrupt(); // restore interrupted state to comply with best practices
    }

    // Additional provisioning (profile creation, default settings) could be added here.
  }
}
