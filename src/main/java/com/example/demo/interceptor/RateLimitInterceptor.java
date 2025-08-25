package com.example.demo.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.lang.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate limiting interceptor using in-memory token bucket algorithm
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);
    
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    // (Memory) In a long‑running process this map can grow unbounded with unique client IDs (IPs).
    // (Prod) Use Caffeine/Guava cache with expireAfterAccess or implement scheduled cleanup.
    // Simple lightweight cleanup hook could iterate occasionally & remove stale buckets based on lastRefillTime.
    
    // Default rate limits
    private static final int AUTH_REQUESTS_PER_MINUTE = 5;
    private static final int API_REQUESTS_PER_MINUTE = 100;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String clientId = getClientIdentifier(request);
        String requestPath = request.getRequestURI();
        
        int limit = isAuthEndpoint(requestPath) ? AUTH_REQUESTS_PER_MINUTE : API_REQUESTS_PER_MINUTE;
        
        TokenBucket bucket = buckets.computeIfAbsent(clientId, k -> new TokenBucket(limit));
        // (Optional) Opportunistic cleanup every N requests (cheap heuristic for demo)
        if (buckets.size() > 1000 && bucket.lastRefillTime.get() % 100 == 0) {
            buckets.entrySet().removeIf(e -> e.getValue().isStale());
        }
        
        if (!bucket.tryConsume()) {
            logger.warn("Rate limit exceeded for client: {} on path: {}", clientId, requestPath);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests\"}");
            return false;
        }
        
        return true;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Use IP address as client identifier
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth");
    }

    /**
     * Simple token bucket implementation
     */
    private static class TokenBucket {
        private final int capacity;
        private final AtomicInteger tokens;
        private final AtomicLong lastRefillTime;
        private final long refillIntervalMs = 60000; // 1 minute
        private final long ttlMs = 5 * 60 * 1000; // (Cleanup) consider bucket stale after 5 minutes of inactivity

        public TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
        }

    public boolean tryConsume() {
            refillIfNeeded();
            
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refillIfNeeded() {
            long now = System.currentTimeMillis();
            long lastRefill = lastRefillTime.get();
            
            if (now - lastRefill >= refillIntervalMs) {
                if (lastRefillTime.compareAndSet(lastRefill, now)) {
                    tokens.set(capacity);
                }
            }
        }
        private boolean isStale() {
            return System.currentTimeMillis() - lastRefillTime.get() > ttlMs;
        }
    }
}
