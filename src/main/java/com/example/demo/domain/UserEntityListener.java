package com.example.demo.domain;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;

public class UserEntityListener {

    @PostPersist
    public void afterSave(User user) {
        System.out.println("[EntityListener] User created: " + user.getEmail());
    }

    @PostUpdate
    public void afterUpdate(User user) {
        System.out.println("[EntityListener] User updated: " + user.getEmail());
    }
}
