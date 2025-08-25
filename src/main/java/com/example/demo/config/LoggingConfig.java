package com.example.demo.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import java.io.IOException;
import java.util.UUID;

/**
 * Configuration for structured logging and tracing
 */
@Configuration
public class LoggingConfig {

    @Bean
    public OncePerRequestFilter requestTrackingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                          @NonNull HttpServletResponse response, 
                                          @NonNull FilterChain filterChain) throws ServletException, IOException {
                
                String requestId = UUID.randomUUID().toString();
                // Avoid creating HTTP session for stateless APIs; only fetch existing session if present.
                // Using getSession(false) prevents unnecessary session objects & memory overhead.
                var session = request.getSession(false);
                String sessionId = session != null ? session.getId() : "N/A"; // (Prod) Omit or hash if considered PII.
                String userAgent = request.getHeader("User-Agent");
                String remoteAddr = getClientIpAddress(request);
                
                try {
                    // Add contextual information to MDC for structured logging
                    MDC.put("requestId", requestId);
                    MDC.put("sessionId", sessionId);
                    // (Security) Consider normalizing / hashing userAgent for privacy if logs exported to third parties.
                    MDC.put("userAgent", userAgent != null ? userAgent : "unknown");
                    MDC.put("clientIp", remoteAddr);
                    MDC.put("requestUri", request.getRequestURI());
                    MDC.put("httpMethod", request.getMethod());
                    
                    // Add request ID to response headers for correlation
                    response.setHeader("X-Request-ID", requestId);
                    // (Prod) Add security headers here or in a dedicated filter (e.g. CSP, Referrer-Policy, Permissions-Policy)
                    // response.setHeader("Content-Security-Policy", "default-src 'self'"); // Example placeholder
                    
                    filterChain.doFilter(request, response);
                    
                } finally {
                    // Clean up MDC to prevent memory leaks
                    MDC.clear();
                }
            }
            
            private String getClientIpAddress(HttpServletRequest request) {
                // (Security) In production, validate trusted proxy chain before honoring X-Forwarded-For to avoid spoofing.
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                return request.getRemoteAddr();
            }
        };
    }
}
