package com.example.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Represents a comment on a post. This entity has a many-to-one relationship with both Post and
 * User (the commenter). It inherits auditing fields from BaseEntity.
 */
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    /**
     * The content of the comment. The @Lob annotation specifies that this should be stored as a Large
     * Object in the database, suitable for long text.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * The post to which this comment belongs. This is the owning side of the many-to-one
     * relationship. FetchType.LAZY is used for performance, so the Post is not loaded until it's
     * explicitly accessed.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id") // Specifies the foreign key column in the 'comments' table.
    private Post post;

    /**
     * The user who wrote the comment. This is another many-to-one relationship, linking the comment
     * to its author. FetchType.LAZY is also used here for performance.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Specifies the foreign key column.
    private User commenter;

    // Getters and Setters

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public User getCommenter() {
        return commenter;
    }

    public void setCommenter(User commenter) {
        this.commenter = commenter;
    }

    /**
     * Overriding equals() based on the entity's ID. This is a common practice for JPA entities to
     * ensure they are compared by identity.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        // If the ID is null, the entities are not equal.
        if (getId() == null || comment.getId() == null) {
            return false;
        }
        return Objects.equals(getId(), comment.getId());
    }

    /**
     * Overriding hashCode() to be consistent with the equals() method. Uses the entity's ID for the
     * hash code.
     */
    @Override
    public int hashCode() {
        // Use a prime number for hashing if the ID is null, otherwise use the ID's hash code.
        return Objects.hashCode(getId());
    }
}
