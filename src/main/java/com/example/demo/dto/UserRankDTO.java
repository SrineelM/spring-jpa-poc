package com.example.demo.dto;

public class UserRankDTO {
    private Long id;
    private String name;
    private String role;
    private String createdAt;
    private Integer roleRank;
    private Long totalInRole;

    public UserRankDTO(Long id, String name, String role, String createdAt, Integer roleRank, Long totalInRole) {
        this.id = id; this.name = name; this.role = role; this.createdAt = createdAt; this.roleRank = roleRank; this.totalInRole = totalInRole;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getCreatedAt() { return createdAt; }
    public Integer getRoleRank() { return roleRank; }
    public Long getTotalInRole() { return totalInRole; }
}
