package com.example.demo.repository;

import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Repository showcasing usage of database window functions (RANK, COUNT OVER) via a native query to
 * compute per-role rankings without multiple round trips.
 */
@Repository
public interface UserWindowRepository extends JpaRepository<com.example.demo.domain.User, Long> {

    @Query(
            value = "SELECT u.id, u.name, u.role, u.created_date, "
                    + "RANK() OVER (PARTITION BY u.role ORDER BY u.created_date DESC) AS role_rank, "
                    + "COUNT(*) OVER (PARTITION BY u.role) AS total_in_role "
                    + "FROM users u",
            nativeQuery = true)
    List<Object[]> getUserRankingRaw(); // raw array: map to DTO (UserRankDTO) at service layer
}
