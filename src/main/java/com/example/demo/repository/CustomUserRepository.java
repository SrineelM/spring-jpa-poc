package com.example.demo.repository;

import com.example.demo.domain.User;
import java.util.List;

public interface CustomUserRepository {
    List<User> findUsingCriteria(String nameLike, String email);
}
