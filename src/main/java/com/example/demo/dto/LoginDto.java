package com.example.demo.dto;

/**
 * Login request payload. Sent by the client when attempting to authenticate.
 * Only contains credentials – no domain logic here; validation (e.g. @NotBlank)
 * can be added later if Bean Validation is introduced.
 */
public class LoginDto {

    /**
     * Email (or username) the user registered with.
     * Acts as principal identifier in authentication.
     */
    private String email;

    /**
     * Raw password as entered by the user. It is never stored directly; the
     * authentication layer will encode & compare it with the hashed password.
     */
    private String password;

    // --- Accessors ---
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
