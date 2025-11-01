package com.example.demo.repository.jdbc;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.dto.UserSummaryDto;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

/**
 * Spring JDBC repository using NamedParameterJdbcTemplate to demonstrate:
 * - Safe, parameterized SQL (SQL injection prevention)
 * - Lightweight read/write operations without JPA overhead
 * - Simple mapping to the existing "users" table
 */
@Repository
public class UserJdbcRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public UserJdbcRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Lightweight projection returning only selected fields to avoid over-fetching */
    @SuppressWarnings("null")
    public Optional<User> findByEmail(String email) {
        try {
            String sql = "SELECT id, email, name, password, role FROM users WHERE email = :email";
            List<User> result = jdbc.query(sql, new MapSqlParameterSource("email", email), userRowMapper());
            return result.stream().findFirst();
        } catch (DataAccessException ex) {
            // In production, add error classification & metrics
            return Optional.empty();
        }
    }

    /** Simple list with pagination via offset/limit */
    @SuppressWarnings("null")
    public List<User> findAll(int offset, int limit) {
        String sql =
                "SELECT id, email, name, password, role FROM users ORDER BY id OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY";
        return jdbc.query(
                sql, new MapSqlParameterSource().addValue("offset", offset).addValue("limit", limit), userRowMapper());
    }

    /** Insert example demonstrating named parameters and protection against SQL injection */
    public int insert(User user) {
        String sql =
                "INSERT INTO users (created_date, last_modified_date, created_by, last_modified_by, email, name, password, role) "
                        + "VALUES (CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, :createdBy, :lastModifiedBy, :email, :name, :password, :role)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("createdBy", "jdbc")
                .addValue("lastModifiedBy", "jdbc")
                .addValue("email", user.getEmail())
                .addValue("name", user.getName())
                .addValue("password", user.getPassword())
                .addValue("role", user.getRole() != null ? user.getRole().name() : Role.USER.name());
        return jdbc.update(sql, params);
    }

    /** Update a user's name by id using safe named parameters */
    public int updateName(Long id, String name) {
        String sql =
                "UPDATE users SET name = :name, last_modified_date = CURRENT_TIMESTAMP, last_modified_by = :by WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("by", "jdbc")
                .addValue("id", id);
        return jdbc.update(sql, params);
    }

    /** Batch update names in one round-trip */
    public int[] batchUpdateNames(Map<Long, String> updates) {
        String sql =
                "UPDATE users SET name = :name, last_modified_date = CURRENT_TIMESTAMP, last_modified_by = :by WHERE id = :id";
        SqlParameterSource[] batch = updates.entrySet().stream()
                .map(e -> new MapSqlParameterSource()
                        .addValue("name", e.getValue())
                        .addValue("by", "jdbc")
                        .addValue("id", e.getKey()))
                .toArray(SqlParameterSource[]::new);
        return jdbc.batchUpdate(sql, batch);
    }

    /** Lightweight DTO projection using RowMapper mapping only id, name, email */
    public List<UserSummaryDto> findSummaries() {
        String sql = "SELECT id, name, email FROM users ORDER BY id";
        return jdbc.query(
                sql, (rs, rowNum) -> new UserSummaryDto(rs.getLong("id"), rs.getString("name"), rs.getString("email")));
    }

    private RowMapper<User> userRowMapper() {
        return (ResultSet rs, int rowNum) -> {
            User u = new User();
            // id is generated and exposed via getter only; we avoid setting it directly
            u.setEmail(rs.getString("email"));
            u.setName(rs.getString("name"));
            u.setPassword(rs.getString("password"));
            String role = rs.getString("role");
            if (role != null) {
                u.setRole(Role.valueOf(role));
            }
            return u;
        };
    }
}
