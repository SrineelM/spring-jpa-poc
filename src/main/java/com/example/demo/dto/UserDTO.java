package com.example.demo.dto;

/**
 * Generic user representation used in API responses where only core identity attributes are
 * required (no internal fields like password / roles list).
 */
public class UserDTO {
  /** Persistent primary key of the user entity. */
  private Long id;

  /** Display / full name. */
  private String name;

  /** Public contact or login email. */
  private String email;

  public UserDTO() {
    /* Default constructor for frameworks (Jackson / JPA projections) */
  }

  public UserDTO(Long id, String name, String email) {
    // Simple assignment constructor used by mapping logic / JPQL constructor expressions
    this.id = id;
    this.name = name;
    this.email = email;
  }

  // --- Accessors ---
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
