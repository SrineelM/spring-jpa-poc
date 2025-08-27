package com.example.demo.dto;

/**
 * Projection DTO representing a user's relative ordering inside a particular role.
 * Useful for leaderboard / ranking style API responses.
 */
public class UserRankDTO {
    private Long id;          // User identifier
    private String name;      // Display name
    private String role;      // Role name (e.g. ADMIN, USER)
    private String createdAt; // Formatted creation timestamp for display (string to avoid tz issues in clients)
    private Integer roleRank; // 1-based rank position among users sharing the same role
    private Long totalInRole; // Total number of users that have this role (for computing percentile)

    public UserRankDTO(Long id, String name, String role, String createdAt, Integer roleRank, Long totalInRole) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
        this.roleRank = roleRank;
        this.totalInRole = totalInRole;
    }

    // --- Accessors (no setters: immutable after construction) ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
    public Integer getRoleRank() { return roleRank; }
    public Long getTotalInRole() { return totalInRole; }
}
