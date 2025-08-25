package com.example.demo.repository;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.UserSummaryDto;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User>, QueryByExampleExecutor<User> {

    /**
     * Finds a user by their email and eagerly fetches their profile in the same query.
     * This is an example of solving the N+1 problem for a one-to-one relationship using a LEFT JOIN FETCH clause.
     *
     * @param email The email of the user to find.
     * @return An Optional containing the User with their profile, if found.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile p WHERE u.email = :email")
    Optional<User> findByEmailWithProfile(@Param("email") String email);

    /**
     * Finds all users with a given role.
     * NOTE: This method is susceptible to the N+1 problem. If you iterate over the returned list
     * and access a lazy association (e.g., user.getPosts()), it will trigger an additional query for each user.
     *
     * @param role The role to search for.
     * @return A list of users with the specified role.
     */
    List<User> findByRole(Role role);

    /**
     * Solves the N+1 problem by fetching all users and their posts in a single query using JOIN FETCH.
     * This is a good approach when you know you will always need the posts.
     * The DISTINCT keyword is important to avoid duplicate User records in the result set due to the join.
     *
     * @return A list of all users with their posts eagerly loaded.
     */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.posts")
    List<User> findAllWithPosts();

    /**
     * Solves the N+1 problem using an @EntityGraph.
     * This approach is cleaner than JOIN FETCH as it separates the query from the fetch plan.
     * The entity graph "User.withProfileAndPosts" is defined in the User entity.
     * Spring Data JPA will automatically generate the appropriate join query based on the graph.
     *
     * @return A list of all users with their profile and posts eagerly loaded.
     */
    @EntityGraph(value = "User.withProfileAndPosts")
    @Query("SELECT u FROM User u")
    List<User> findAllWithProfileAndPosts();

    /**
     * Demonstrates a DTO projection.
     * This query fetches only the id, name, and email from the User entity and directly instantiates
     * a UserSummaryDto for each result. This is highly efficient because it avoids fetching the entire
     * entity and its associations, reducing the amount of data transferred from the database.
     *
     * @return A list of UserSummaryDto objects.
     */
    @Query("SELECT new com.example.demo.dto.UserSummaryDto(u.id, u.name, u.email) FROM User u")
    List<UserSummaryDto> findAllUserSummaries();


    @Procedure(name = "User.countByRole")
    Long countPostsForRole(@Param("role_in") String role);

    @Modifying
    @Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
    int updateName(@Param("id") Long id, @Param("name") String name);

    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<User> searchByNameLike(@Param("term") String term);
}
