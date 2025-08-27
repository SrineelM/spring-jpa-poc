package com.example.demo.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Demonstrates a straightforward synchronous event listener. Because no @Async is used, publisher
 * and listener run on the same thread; failures here will propagate back to the publisher (decide
 * if that is desirable for your domain).
 */
@Component
public class UserCreatedListener {

  @EventListener
  public void onUserCreated(UserCreatedEvent event) {
    // Minimal side effect (stdout). Replace with structured logging or downstream calls
    // if needed. Keeping it light ensures creation path stays fast.
    System.out.println("[EventListener] User created event: " + event.getUser().getEmail());
  }
}
