package com.example.demo.dto;

/**
 * A Data Transfer Object (DTO) representing a summary of a User.
 * This is used for DTO projections, which are an efficient way to fetch
 * only the required data from the database, reducing the payload and improving performance.
 */
public class UserSummaryDto {

    private final Long id;
    private final String name;
    private final String email;

    /**
     * This constructor is used by JPA's constructor expression in JPQL queries.
     * It allows JPA to directly instantiate this DTO from a query result.
     *
     * @param id The user's ID.
     * @param name The user's name.
     * @param email The user's email.
     */
    public UserSummaryDto(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
