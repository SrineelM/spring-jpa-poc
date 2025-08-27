package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a group of users. This entity has a many-to-many relationship with the User entity.
 */
@Entity
@Table(name = "groups") // It's good practice to use plural for table names
public class Group extends BaseEntity {

    /** The name of the group. */
    private String name;

    /**
     * The set of users who are members of this group. This is the non-owning side of the many-to-many
     * relationship, indicated by the `mappedBy = "groups"` attribute. The `groups` field is defined
     * in the User entity, which is the owning side.
     */
    @ManyToMany(mappedBy = "groups")
    private Set<User> users = new HashSet<>();

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    // It's good practice to provide a setter for collections as well,
    // though it should be used with care.
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    /** Overriding equals() based on the group's name, which is assumed to be a business key. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(name, group.name);
    }

    /** Overriding hashCode() to be consistent with the equals() method. */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
