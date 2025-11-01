package com.example.demo.config;

import java.util.List;
import java.util.Optional;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration for JPA and Caching. This class sets up beans related to persistence and caching
 * layers.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

    /**
     * Provides the current auditor of the application. This is used by Spring Data JPA to
     * automatically populate @CreatedBy and @LastModifiedBy fields. In a real application, this would
     * be integrated with Spring Security to return the currently logged-in user.
     *
     * @return AuditorAware bean that provides the current user's identifier.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        // A lambda expression that returns the current auditor.
        // It attempts to get the username from the Spring Security context.
        // If no user is authenticated (e.g., during system startup or for anonymous access),
        // it defaults to "system_user".
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system_user");
            }
            return Optional.ofNullable(authentication.getName());
        };
    }

    /**
     * Configures the CacheManager for the application using Caffeine. Caffeine is a high-performance,
     * in-memory caching library. Caching is used here to reduce database load and improve application
     * performance by storing frequently accessed data in memory.
     *
     * @return A configured CaffeineCacheManager.
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cm = new CaffeineCacheManager();
        // Defines the names of the caches to be used in the application.
        // These names correspond to the `cacheNames` attribute in @Cacheable, @CachePut, etc.
        cm.setCacheNames(List.of("users", "posts", "user_details"));
        // Note: Further Caffeine configuration (e.g., size, expiration) can be set here
        // using a CaffeineSpec. For example:
        // cm.setCaffeineSpec(CaffeineSpec.parse("maximumSize=100,expireAfterWrite=10m"));
        return cm;
    }
}
