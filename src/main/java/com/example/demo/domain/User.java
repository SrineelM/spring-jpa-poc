package com.example.demo.domain;

import jakarta.persistence.*;
import java.util.*;

/**
 * Represents a user in the system.
 * This entity is mapped to the "users" table and includes relationships
 * with other entities like Profile, Post, and Group. It serves as a central
 * part of the domain model.
 */
@Entity // Declares this class as a JPA entity.
@Table(name = "users") // Specifies the table name in the database.
@NamedQuery(name = "User.findByRole", query = "SELECT u FROM User u WHERE u.role = :role") // Defines a static JPQL query.
@NamedStoredProcedureQuery( // Defines a mapping for a stored procedure.
    name = "User.countByRole",
    procedureName = "COUNT_USERS_BY_ROLE",
    parameters = {
        @StoredProcedureParameter(mode = ParameterMode.IN, name = "role_in", type = String.class),
        @StoredProcedureParameter(mode = ParameterMode.OUT, name = "count_out", type = Long.class)
    }
)
@EntityListeners({AuditEntityListener.class, UserEntityListener.class}) // Registers entity listeners for this entity.
// Defines an entity graph to solve the N+1 query problem.
// This graph specifies that when fetching a User, their associated Profile and Posts should also be fetched in the same query.
@NamedEntityGraph(
    name = "User.withProfileAndPosts",
    attributeNodes = {
        @NamedAttributeNode("profile"),
        @NamedAttributeNode("posts")
    }
)
public class User extends BaseEntity {

    /**
     * The user's email, which is unique and used for login.
     * Marked as non-nullable and unique to ensure data integrity.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * The user's full name.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The user's hashed password.
     * Should never be stored in plain text.
     */
    @Column(nullable = false)
    private String password;

    /**
     * The user's role, which determines their permissions.
     * Stored as a string in the database for readability.
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * The user's embedded address information.
     * The Address class is an @Embeddable, so its fields are part of the users table.
     */
    @Embedded
    private Address address;

    /**
     * The user's profile, with a one-to-one relationship.
     * FetchType is LAZY by default for one-to-one, which is good practice.
     * However, if you fetch a list of users and then access their profiles in a loop,
     * it will trigger an N+1 query problem (1 query for users, N queries for profiles).
     * To solve this, use a JOIN FETCH query or an @EntityGraph.
     * `cascade = CascadeType.ALL` means operations (persist, merge, remove, etc.) on User will propagate to Profile.
     * `orphanRemoval = true` means that if a Profile is disassociated from a User, it should be deleted.
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Profile profile;

    /**
     * The list of posts authored by the user.
     * FetchType is LAZY by default for one-to-many, which is essential for performance.
     * Accessing this collection without an explicit fetch strategy will lead to the N+1 problem.
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    /**
     * The list of comments made by the user.
     */
    @OneToMany(mappedBy = "commenter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    /**
     * The set of groups the user belongs to.
     * FetchType is LAZY by default for many-to-many.
     * A join table `user_groups` is used to manage this relationship.
     */
    @ManyToMany
    @JoinTable(name = "user_groups",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups = new HashSet<>();

    /**
     * A collection of simple string tags associated with the user.
     * This is an example of an element collection, where the elements are basic types, not entities.
     */
    @ElementCollection
    @CollectionTable(name = "user_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    /**
     * A temporary token used for operations like password reset.
     * This field is not persisted to the database, as indicated by the @Transient annotation.
     */
    @Transient
    private String temporaryToken;

    // Getters and Setters

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) {
        if (profile == null) {
            if (this.profile != null) {
                this.profile.setUser(null);
            }
        } else {
            profile.setUser(this);
        }
        this.profile = profile;
    }
    public List<Post> getPosts() { return posts; }
    public List<Comment> getComments() { return comments; }
    public Set<Group> getGroups() { return groups; }
    public Set<String> getTags() { return tags; }
    public void setTemporaryToken(String t) { this.temporaryToken = t; }

    // Helper methods for maintaining bidirectional relationships.
    // These methods ensure that both sides of the relationship are in sync.

    public void addPost(Post post) {
        posts.add(post);
        post.setAuthor(this);
    }

    public void removePost(Post post) {
        posts.remove(post);
        post.setAuthor(null);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setCommenter(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setCommenter(null);
    }

    public void addGroup(Group group) {
        groups.add(group);
        group.getUsers().add(this);
    }

    public void removeGroup(Group group) {
        groups.remove(group);
        group.getUsers().remove(this);
    }

    /**
     * Overriding equals() based on the business key (email).
     * This is important for entities to behave correctly in collections and when attached/detached from the persistence context.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    /**
     * Overriding hashCode() to be consistent with the equals() method.
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
