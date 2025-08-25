package com.example.demo.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class UserCreatedListener {

    @EventListener
    public void onUserCreated(UserCreatedEvent event) {
        System.out.println("[EventListener] User created event: " + event.getUser().getEmail());
    }
}
