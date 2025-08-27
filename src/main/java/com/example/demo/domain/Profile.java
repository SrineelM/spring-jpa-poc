package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Represents a user's profile.
 * This entity has a one-to-one relationship with the User entity using a shared primary key.
 * This means the primary key of the 'profiles' table is also a foreign key to the 'users' table.
 */
@Entity
@Table(name = "profiles")
public class Profile {

    /**
     * The primary key of the Profile entity.
     * It is not auto-generated; instead, it's mapped from the associated User entity's ID.
     */
    @Id
    private Long id;

    /**
     * The User associated with this profile.
     * The @OneToOne annotation defines the relationship.
     * The @MapsId annotation indicates that the primary key of this entity (Profile)
     * is mapped from the User entity. The value of Profile's 'id' will be the same as
     * the 'id' of the User it's associated with.
     * The @JoinColumn specifies that the 'user_id' column in the 'profiles' table
     * is both the primary key and the foreign key.
     */
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * A short biography of the user.
     * Stored as a Large Object (Lob) to accommodate longer text.
     */
    @Lob
    private String bio;

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    /**
     * Overriding equals() based on the entity's ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Profile profile = (Profile) o;
        return Objects.equals(id, profile.id);
    }

    /**
     * Overriding hashCode() to be consistent with the equals() method.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
