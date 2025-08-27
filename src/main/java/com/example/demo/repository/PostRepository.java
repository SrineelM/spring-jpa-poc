package com.example.demo.repository;

import com.example.demo.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Standard Spring Data repository for Post entities. Inherits CRUD + paging
 * operations. Add derived queries or custom fragments as needed.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> { }
