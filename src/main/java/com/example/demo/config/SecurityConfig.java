package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationEntryPoint;
import com.example.demo.security.JwtAuthenticationFilter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Enhanced security configuration with comprehensive security headers and CORS. This class
 * configures Spring Security for a stateless JWT-based authentication system.
 *
 * <p>Key features: - JWT-based stateless authentication - CORS configuration for cross-origin
 * requests - Security headers for protection against common attacks - Role-based authorization with
 * method-level security - Custom authentication entry point for unauthorized requests
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

  // Dependencies injected via constructor
  private final JwtAuthenticationEntryPoint
      authenticationEntryPoint; // Handles unauthorized access attempts
  private final JwtAuthenticationFilter
      authenticationFilter; // Processes JWT tokens on incoming requests

  /**
   * Constructor for dependency injection. Spring will automatically provide instances of the
   * required beans.
   */
  public SecurityConfig(
      JwtAuthenticationEntryPoint authenticationEntryPoint,
      JwtAuthenticationFilter authenticationFilter) {
    this.authenticationEntryPoint = authenticationEntryPoint;
    this.authenticationFilter = authenticationFilter;
  }

  /**
   * Defines a PasswordEncoder bean for encoding and verifying passwords. BCrypt is used for its
   * strength and built-in salt generation.
   *
   * @return A BCryptPasswordEncoder with strength 12 (higher = more secure but slower)
   */
  @Bean // Defines this method as a Spring bean provider
  public static PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // Increased strength from default 10 for better security
  }

  /**
   * Defines an AuthenticationManager bean. This is required for authenticating users
   * programmatically.
   *
   * @param configuration The authentication configuration provided by Spring
   * @return The configured AuthenticationManager
   * @throws Exception If configuration fails
   */
  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  /**
   * Defines a CORS configuration source. CORS (Cross-Origin Resource Sharing) allows the API to be
   * accessed from different domains.
   *
   * @return A CorsConfigurationSource with permissive settings for development
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Allow requests from localhost on any port (for development)
    configuration.setAllowedOriginPatterns(
        Arrays.asList("http://localhost:*", "https://localhost:*"));
    // Allow common HTTP methods
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    // Allow all headers
    configuration.setAllowedHeaders(Arrays.asList("*"));
    // Allow credentials (cookies, authorization headers)
    configuration.setAllowCredentials(true);
    // Cache preflight response for 1 hour
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    // Apply CORS configuration to all /api/** endpoints
    source.registerCorsConfiguration("/api/**", configuration);
    return source;
  }

  /**
   * Defines the main security filter chain. This method configures how HTTP requests are secured.
   *
   * @param http The HttpSecurity object to configure
   * @return A configured SecurityFilterChain
   * @throws Exception If configuration fails
   */
  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // Disable CSRF protection (common for stateless APIs with JWT)
        .csrf(csrf -> csrf.disable())
        // Enable CORS with our custom configuration
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // Enhanced security headers
        .headers(
            headers ->
                headers
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .contentTypeOptions(contentTypeOptions -> {})
                    .httpStrictTransportSecurity(
                        hstsConfig -> hstsConfig.maxAgeInSeconds(31536000).includeSubDomains(true))
            // (Prod) Consider adding Content-Security-Policy, Referrer-Policy, Permissions-Policy,
            // X-Content-Type-Options (auto),
            // Cross-Origin-Opener-Policy, and removing any unnecessary headers.
            )
        .authorizeHttpRequests(
            authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .requestMatchers("/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/actuator/info")
                    .permitAll()
                    .requestMatchers("/h2-console/**")
                    .permitAll() // (Dev Only) Disable & remove in production
                    .requestMatchers("/error")
                    .permitAll()

                    // Admin only endpoints
                    .requestMatchers("/actuator/**")
                    .hasRole("ADMIN")

                    // Authenticated endpoints
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
        .sessionManagement(
            session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false));

    // Add JWT filter
    http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}
