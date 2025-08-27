package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Enhanced JWT token provider with comprehensive validation and error handling
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    private Key key() {
        // (Security) Secret should be 64+ bytes for HS512 and sourced from environment / vault, not plain properties.
        // (Key Rotation) For production, implement a KeyResolver that selects signing/verification keys by key id (kid) header.
        // Example approach (omitted for brevity): maintain Map<String, Key> activeKeys; add .setHeaderParam("kid", currentKeyId) when building token.
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a JWT with enhanced claims and security
     */
    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Date currentDate = new Date();
    Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate); // compute absolute expiration timestamp

        logger.debug("Generating JWT token for user: {}", username);

    return Jwts.builder()
        .setSubject(username)
        .setIssuedAt(currentDate)
        .setExpiration(expireDate)
        .setIssuer("spring-jpa-poc")
        // (PII) Be cautious adding user identifiable claims; keep minimal.
        .claim("authorities", authentication.getAuthorities())
        .claim("tokenType", "access")
        // (Rotation) Add .setHeaderParam("kid", "v1") when multiple keys are supported.
        .signWith(key(), SignatureAlgorithm.HS512) // Stronger algorithm
        .compact();
    }

    /**
     * Enhanced username extraction with validation
     */
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            String username = claims.getSubject(); // subject claim typically stores principal identifier
            logger.debug("Extracted username from token: {}", username);
            return username;
            
        } catch (Exception e) {
            logger.error("Failed to extract username from token", e);
            throw new JwtAuthenticationException("Invalid token: unable to extract username");
        }
    }

    /**
     * Comprehensive token validation with detailed error handling
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            // Additional validation
            if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
                logger.warn("Token validation failed: Subject is null or empty");
                return false;
            }
            
            if (claims.getExpiration().before(new Date())) { // explicit expiration check (library also throws ExpiredJwtException)
                logger.warn("Token validation failed: Token is expired");
                return false;
            }
            
            logger.debug("Token validation successful for subject: {}", claims.getSubject());
            return true;
            
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during token validation", e);
        }
        
        return false;
    }

    /**
     * Get token expiration time
     */
    public Date getExpirationDateFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            logger.error("Failed to extract expiration date from token", e);
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }
}

/**
 * Custom exception for JWT authentication failures
 */
class JwtAuthenticationException extends RuntimeException {
    public JwtAuthenticationException(String message) {
        super(message);
    }
    
    public JwtAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
