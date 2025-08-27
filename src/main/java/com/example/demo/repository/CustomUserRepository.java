package com.example.demo.repository;

import com.example.demo.domain.User;
import java.util.List;

/**
 * Custom repository fragment exposing ad-hoc dynamic criteria-based lookup not
 * easily expressed as a Spring Data derived query method. Implemented in
 * {@link CustomUserRepositoryImpl} and automatically composed into Spring Data
 * repository if extended.
 */
public interface CustomUserRepository {
    /**
     * Dynamically builds a criteria query filtering by optional name/email parameters.
     * @param nameLike substring (case-insensitive) to match against user.name (nullable)
     * @param email exact email (case-insensitive) to match (nullable)
     * @return users satisfying provided filters
     */
    List<User> findUsingCriteria(String nameLike, String email);
}
