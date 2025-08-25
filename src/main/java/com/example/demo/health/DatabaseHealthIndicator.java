package com.example.demo.health;

import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Proper Actuator HealthIndicator for database connectivity with timing & rich details.
 * Exposes status under /actuator/health (and groups) enabling aggregation & alerting.
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthIndicator.class);

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @Timed(value = "health.db", description = "Time taken to perform DB health check")
    public Health health() {
        long start = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection()) {
            boolean valid = connection.isValid(1); // 1 second validation timeout
            long duration = System.currentTimeMillis() - start;
            if (valid) {
                return Health.up()
                        .withDetail("validated", true)
                        .withDetail("validationTimeMs", duration)
                        .build();
            } else {
                return Health.down()
                        .withDetail("validated", false)
                        .withDetail("validationTimeMs", duration)
                        .build();
            }
        } catch (SQLException ex) {
            long duration = System.currentTimeMillis() - start;
            logger.error("Database health check failed", ex);
            return Health.down(ex)
                    .withDetail("validated", false)
                    .withDetail("validationTimeMs", duration)
                    .withDetail("sqlState", ex.getSQLState())
                    .withDetail("errorCode", ex.getErrorCode())
                    .build();
        }
    }

    // Convenience methods retained for existing controller usage (backward compatibility)
    public boolean isHealthy() { return health().getStatus().getCode().equalsIgnoreCase("UP"); }
    public String getHealthStatus() { return health().getStatus().getCode(); }
}
