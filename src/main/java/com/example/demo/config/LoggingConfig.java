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
 * Configuration for structured logging and tracing.
 * This class sets up a filter that adds contextual information to each HTTP request,
 * enabling better observability and debugging through correlated logs.
 * 
 * Key features:
 * - Generates unique request IDs for tracing
 * - Adds client information to MDC (Mapped Diagnostic Context) for structured logging
 * - Sets response headers for correlation
 * - Handles IP address extraction with proxy support
 * - Cleans up MDC to prevent memory leaks
 */
@Configuration // Marks this class as a source of bean definitions for Spring
public class LoggingConfig {

    /**
     * Creates a OncePerRequestFilter bean that adds logging context to each HTTP request.
     * This filter runs once per request and is ideal for adding request-scoped information.
     * 
     * @return A configured OncePerRequestFilter for request tracking
     */
    @Bean // Defines this method as a Spring bean provider
    public OncePerRequestFilter requestTrackingFilter() {
        return new OncePerRequestFilter() {
            /**
             * Processes each HTTP request to add logging context.
             * This method is called for every incoming request.
             * 
             * @param request The HTTP servlet request
             * @param response The HTTP servlet response
             * @param filterChain The filter chain to continue processing
             * @throws ServletException If a servlet error occurs
             * @throws IOException If an I/O error occurs
             */
            @Override
            protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                          @NonNull HttpServletResponse response, 
                                          @NonNull FilterChain filterChain) throws ServletException, IOException {
                
                // Generate a unique identifier for this request to enable correlation across logs
                String requestId = UUID.randomUUID().toString();
                
                // Avoid creating HTTP session for stateless APIs; only fetch existing session if present.
                // Using getSession(false) prevents unnecessary session objects & memory overhead.
                var session = request.getSession(false);
                String sessionId = session != null ? session.getId() : "N/A"; // (Prod) Omit or hash if considered PII.
                
                // Extract user agent and client IP for logging context
                String userAgent = request.getHeader("User-Agent");
                String remoteAddr = getClientIpAddress(request);
                
                try {
                    // Add contextual information to MDC for structured logging
                    // MDC allows log messages to include request-specific data automatically
                    MDC.put("requestId", requestId); // Unique ID for correlating logs across services
                    MDC.put("sessionId", sessionId); // Session ID if available
                    // (Security) Consider normalizing / hashing userAgent for privacy if logs exported to third parties.
                    MDC.put("userAgent", userAgent != null ? userAgent : "unknown"); // Client browser/app info
                    MDC.put("clientIp", remoteAddr); // Client IP address
                    MDC.put("requestUri", request.getRequestURI()); // The requested URI
                    MDC.put("httpMethod", request.getMethod()); // HTTP method (GET, POST, etc.)
                    
                    // Add request ID to response headers for correlation
                    // This allows clients to correlate their requests with server logs
                    response.setHeader("X-Request-ID", requestId);
                    // (Prod) Add security headers here or in a dedicated filter (e.g. CSP, Referrer-Policy, Permissions-Policy)
                    // response.setHeader("Content-Security-Policy", "default-src 'self'"); // Example placeholder
                    
                    // Continue processing the request through the filter chain
                    filterChain.doFilter(request, response);
                    
                } finally {
                    // Clean up MDC to prevent memory leaks and cross-request contamination
                    MDC.clear();
                }
            }
            
            /**
             * Extracts the client's real IP address, considering proxy headers.
             * This method handles common proxy scenarios like load balancers or CDNs.
             * 
             * @param request The HTTP servlet request
             * @return The client's IP address as a string
             */
            private String getClientIpAddress(HttpServletRequest request) {
                // (Security) In production, validate trusted proxy chain before honoring X-Forwarded-For to avoid spoofing.
                // Check X-Forwarded-For header first (set by proxies/load balancers)
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim(); // Take the first IP if multiple
                }
                // Fallback to X-Real-IP header (used by some proxies like nginx)
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                // Final fallback to the remote address from the socket
                return request.getRemoteAddr();
            }
        };
    }
}
