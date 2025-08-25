package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * This filter intercepts incoming HTTP requests to validate the JWT in the
 * Authorization header. If the token is valid, it sets the user's authentication
 * in the Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method is executed for each incoming request. It extracts the JWT,
     * validates it, and sets the user's authentication if the token is valid.
     *
     * @param request The incoming HTTP request.
     * @param response The HTTP response.
     * @param filterChain The filter chain.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. Extract the JWT from the request header.
        String token = getJWTfromRequest(request);

        // 2. Validate the token.
        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            // 3. Get the username from the token.
            String username = tokenProvider.getUsername(token);

            // 4. Load the user details from the database.
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 5. Create an authentication token.
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

            // 6. Set additional details for the authentication token.
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 7. Set the authentication in the SecurityContextHolder.
            // This is how Spring Security knows the current user is authenticated.
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        // 8. Continue the filter chain.
        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT from the "Authorization" header of the request.
     *
     * @param request The HTTP request.
     * @return The JWT string, or null if not found.
     */
    private String getJWTfromRequest(HttpServletRequest request) {
        // The token is expected to be in the format: "Bearer <token>"
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract the token part from the header.
            return bearerToken.substring(7);
        }
        return null;
    }
}
