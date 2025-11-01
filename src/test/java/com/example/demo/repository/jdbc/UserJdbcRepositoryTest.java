package com.example.demo.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.User;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@JdbcTest
@Import({UserJdbcRepository.class, UserJdbcRepositoryTest.Config.class})
class UserJdbcRepositoryTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class Config {
        @org.springframework.context.annotation.Bean
        NamedParameterJdbcTemplate namedParameterJdbcTemplate(javax.sql.DataSource ds) {
            return new NamedParameterJdbcTemplate(ds);
        }
    }

    @Autowired private UserJdbcRepository repo;

    @Test
    void findByEmail_returnsUser_whenExists() {
        Optional<User> u = repo.findByEmail("user1@example.com");
        assertThat(u).isPresent();
        assertThat(u.get().getEmail()).isEqualTo("user1@example.com");
        assertThat(u.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotFound() {
        Optional<User> u = repo.findByEmail("missing@example.com");
        assertThat(u).isEmpty();
    }
}
