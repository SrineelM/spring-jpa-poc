package com.example.demo.dto;

/**
 * Lightweight user projection used when only the essential identifying
 * information is needed (e.g. list views). Avoids fetching heavier relations.
 */
public class UserSummaryDto {

    private final Long id;    // PK
    private final String name; // Display name
    private final String email; // Contact / login email

    /**
     * Constructor leveraged by JPQL constructor expressions: SELECT new ...UserSummaryDto(u.id, u.name, u.email)
     */
    public UserSummaryDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
