package com.example.demo.dto;

/**
 * Registration request payload. Captures the minimal attributes required to create a new user
 * account. Additional profile fields could be added later.
 */
public class SignUpDto {

    /** Human‑readable display name shown in UI / audit logs. */
    private String name;

    /**
     * Unique email which doubles as the login credential. Chosen over a separate username to simplify
     * UX. Uniqueness constraint is enforced at persistence layer.
     */
    private String email;

    /**
     * Plain text password from client. Will be encoded before persisting; NEVER log this value.
     * Consider adding validation annotations (length, complexity).
     */
    private String password;

    // --- Accessors ---
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
