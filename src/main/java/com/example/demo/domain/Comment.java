package com.example.demo.domain;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * Represents a comment on a post.
 * This entity has a many-to-one relationship with both Post and User (the commenter).
 */
@Entity
@Table(name = "comments")
public class Comment extends BaseEntity {

    /**
     * The content of the comment.
     */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * The post to which this comment belongs.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * The user who wrote the comment.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(getId(), comment.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
