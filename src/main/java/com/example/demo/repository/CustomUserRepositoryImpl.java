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


@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> findUsingCriteria(String nameLike, String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> q = cb.createQuery(User.class);
        Root<User> root = q.from(User.class);
        List<Predicate> preds = new ArrayList<>();
        if (nameLike != null) {
            preds.add(cb.like(cb.lower(root.get("name")), "%" + nameLike.toLowerCase() + "%"));
        }
        if (email != null) {
            preds.add(cb.equal(cb.lower(root.get("email")), email.toLowerCase()));
        }
        q.select(root).where(preds.toArray(new Predicate[0]));
        TypedQuery<User> tq = em.createQuery(q);
        return tq.getResultList();
    }
}
