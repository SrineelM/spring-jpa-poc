package com.example.demo.repository;

import com.example.demo.domain.User;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;


@Repository // marks as Spring bean so fragment can be composed into higher level repositories
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em; // injected JPA context for building criteria queries

    @Override
    public List<User> findUsingCriteria(String nameLike, String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder(); // entry point for criteria API
        CriteriaQuery<User> q = cb.createQuery(User.class);
        Root<User> root = q.from(User.class); // FROM User u
        List<Predicate> preds = new ArrayList<>(); // dynamic predicates list
        if (nameLike != null) {
            preds.add(cb.like(cb.lower(root.get("name")), "%" + nameLike.toLowerCase() + "%")); // case-insensitive LIKE
        }
        if (email != null) {
            preds.add(cb.equal(cb.lower(root.get("email")), email.toLowerCase())); // equality on lowered email
        }
        q.select(root).where(preds.toArray(new Predicate[0])); // WHERE clauses aggregated
        TypedQuery<User> tq = em.createQuery(q); // build typed query
        return tq.getResultList(); // execute & return results
    }
}
