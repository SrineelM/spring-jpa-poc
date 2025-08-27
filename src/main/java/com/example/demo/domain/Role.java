package com.example.demo.domain;

/**
 * Represents the roles a user can have in the system. Using an enum for roles improves type safety
 * and code readability compared to using simple strings.
 */
public enum Role {
  /** Standard user with basic permissions. */
  USER,

  /** Administrator with elevated permissions. */
  ADMIN
}
