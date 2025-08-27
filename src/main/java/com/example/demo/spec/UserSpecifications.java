package com.example.demo.spec;

import com.example.demo.domain.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * A utility class that creates reusable {@link Specification} objects for querying the {@link User}
 * entity.
 *
 * <p>The {@link Specification} interface is part of Spring Data JPA and is built on top of the JPA
 * Criteria API. It allows you to define query conditions programmatically, resulting in type-safe,
 * dynamic, and reusable query logic.
 *
 * <p>Each method in this class returns a {@code Specification<User>} that encapsulates a single
 * piece of query logic. These specifications can be chained together using {@code .and()} or {@code
 * .or()} to build complex queries in a fluent and readable way.
 *
 * <p>For example, to find all admin users whose name contains "john":
 *
 * <pre>{@code
 * Specification<User> spec = Specification.where(UserSpecifications.nameContains("john"))
 *                                         .and(UserSpecifications.roleIs("ADMIN"));
 * userRepository.findAll(spec);
 * }</pre>
 */
public class UserSpecifications {

  /**
   * Creates a specification to find users by their exact email address (case-insensitive).
   *
   * @param email The email address to search for. If null, this specification will be ignored.
   * @return A {@link Specification} that can be used in a query. Returns a specification that
   *     always evaluates to true if the email is null.
   * @see Specification
   */
  public static Specification<User> hasEmail(String email) {
    // The lambda expression is the implementation of the Specification's toPredicate method.
    // It receives three arguments from the JPA provider:
    // 1. root: A Root<T> object that represents the entity being queried (User in this case). It's
    // used to access entity attributes.
    // 2. query: A CriteriaQuery<?> object that you can use to add ordering or other query details.
    // 3. cb: A CriteriaBuilder object, which is a factory for creating query predicates (the WHERE
    // clause conditions).
    return (root, query, cb) -> {
      // If the provided email is null, we don't want to add any condition to the query.
      // Returning a null Predicate tells Spring Data JPA to ignore this part of the specification.
      if (email == null || email.isBlank()) {
        return null;
      }

      // Create a Predicate (a single condition in the WHERE clause).
      // 1. cb.lower(root.get("email")): Converts the "email" attribute of the User entity to
      // lowercase.
      // 2. email.toLowerCase(): Converts the input email to lowercase.
      // 3. cb.equal(...): Creates an equality condition (e.g., WHERE lower(email) =
      // 'test@example.com').
      return cb.equal(cb.lower(root.get("email")), email.toLowerCase());
    };
  }

  /**
   * Creates a specification to find users whose name contains a given search term
   * (case-insensitive).
   *
   * @param term The search term to look for within the user's name. If null or blank, this
   *     specification is ignored.
   * @return A {@link Specification} for the query.
   * @see Specification
   */
  public static Specification<User> nameContains(String term) {
    return (root, query, cb) -> {
      if (term == null || term.isBlank()) {
        return null;
      }
      // Create a "LIKE" predicate to find names containing the search term.
      // The "%%" wildcards match any sequence of characters.
      // e.g., WHERE lower(name) LIKE '%john%'
      return cb.like(cb.lower(root.get("name")), "%" + term.toLowerCase() + "%");
    };
  }

  /**
   * Creates a specification to find users by their role.
   *
   * @param roleStr The role to search for (e.g., "ADMIN", "USER"). If null or blank, this
   *     specification is ignored.
   * @return A {@link Specification} for the query.
   * @see Specification
   */
  public static Specification<User> roleIs(String roleStr) {
    return (root, query, cb) -> {
      if (roleStr == null || roleStr.isBlank()) {
        return null;
      }
      // Create a predicate that checks for equality on the "role" field.
      // Note: This assumes the 'role' attribute is an Enum or String that can be directly compared.
      return cb.equal(root.get("role"), roleStr);
    };
  }
}
