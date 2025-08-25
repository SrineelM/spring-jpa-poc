package com.example.demo.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a blog post in the system.
 * This entity is mapped to the "posts" table and has a many-to-one
 * relationship with the User entity (the author).
 */
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

    /**
     * The title of the post.
     */
    private String title;

    /**
     * The content of the post, stored as a large object (TEXT).
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * The author of the post.
     * This is a many-to-one relationship, fetched lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User author;

    /**
     * The list of comments on this post.
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Getters and Setters

    public String getTitle() { return title; }
    public void setTitle(String t) { this.title = t; }
    public String getContent() { return content; }
    public void setContent(String c) { this.content = c; }
    public User getAuthor() { return author; }
    public void setAuthor(User a) { this.author = a; }
    public List<Comment> getComments() { return comments; }

    // Helper methods for bidirectional relationships

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPost(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(getId(), post.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
