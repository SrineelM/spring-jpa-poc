package com.example.demo.dto;

/**
 * A Data Transfer Object (DTO) that represents the data required for a user to log in.
 * This object is used as the request body for the user login endpoint.
 */
public class LoginDto {

    /**
     * The user's email address, which is used as the username for authentication.
     */
    private String email;

    /**
     * The user's password.
     */
    private String password;

    // Getters and Setters

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
