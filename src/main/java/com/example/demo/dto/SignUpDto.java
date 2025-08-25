package com.example.demo.dto;

/**
 * A Data Transfer Object (DTO) that represents the data required to register a new user.
 * This object is used as the request body for the user registration endpoint.
 */
public class SignUpDto {

    /**
     * The user's full name.
     */
    private String name;

    /**
     * The user's email address. This will be used as the username for login.
     */
    private String email;

    /**
     * The user's password.
     */
    private String password;

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
