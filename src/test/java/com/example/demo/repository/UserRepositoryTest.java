package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repo;

    @Test
    void basicSaveAndFind() {
        User u = new User();
        u.setName("John");
        u.setEmail("john@example.com");
        u.setRole(Role.USER);
        repo.save(u);
        assertThat(repo.findByEmailWithProfile("john@example.com")).isPresent();
    }
}
