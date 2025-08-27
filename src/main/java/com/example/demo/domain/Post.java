package com.example.demo.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a blog post in the system. This entity is mapped to the "posts" table and has a
 * many-to-one relationship with the User entity (the author), and a one-to-many relationship with
 * the Comment entity.
 */
@Entity
@Table(name = "posts")
public class Post extends BaseEntity {

  /** The title of the post. */
  private String title;

  /**
   * The content of the post, stored as a large object (TEXT). The @Lob annotation is used for
   * fields that can store large amounts of data.
   */
  @Lob
  @Column(columnDefinition = "TEXT")
  private String content;

  /**
   * The author of the post. This is the owning side of the many-to-one relationship with User.
   * FetchType.LAZY ensures that the author is not loaded from the database until it is explicitly
   * accessed, which is a performance best practice.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id") // Foreign key column in the 'posts' table.
  private User author;

  /**
   * The list of comments on this post. This is the non-owning side of the one-to-many relationship
   * with Comment. `cascade = CascadeType.ALL` means that operations on a Post (e.g., persist,
   * remove) will be cascaded to its comments. `orphanRemoval = true` ensures that if a comment is
   * removed from this list, it is also deleted from the database.
   */
  @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Comment> comments = new ArrayList<>();

  // Getters and Setters

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public List<Comment> getComments() {
    return comments;
  }

  // Helper methods for maintaining the bidirectional relationship with Comment.
  // These methods ensure that both sides of the relationship are kept in sync.

  public void addComment(Comment comment) {
    comments.add(comment);
    comment.setPost(this);
  }

  public void removeComment(Comment comment) {
    comments.remove(comment);
    comment.setPost(null);
  }

  /** Overriding equals() based on the entity's ID. */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Post post = (Post) o;
    if (getId() == null || post.getId() == null) {
      return false;
    }
    return Objects.equals(getId(), post.getId());
  }

  /** Overriding hashCode() to be consistent with the equals() method. */
  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }
}
