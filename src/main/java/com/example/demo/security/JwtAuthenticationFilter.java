package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * This filter intercepts incoming HTTP requests to validate the JWT in the Authorization header. If
 * the token is valid, it sets the user's authentication in the Spring Security context.
 */
@Component // ensures a single execution per request (OncePerRequestFilter base class guarantees)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider tokenProvider;
  private final UserDetailsService userDetailsService;

  public JwtAuthenticationFilter(
      JwtTokenProvider tokenProvider, UserDetailsService userDetailsService) {
    this.tokenProvider = tokenProvider;
    this.userDetailsService = userDetailsService;
  }

  /**
   * This method is executed for each incoming request. It extracts the JWT, validates it, and sets
   * the user's authentication if the token is valid.
   *
   * @param request The incoming HTTP request.
   * @param response The HTTP response.
   * @param filterChain The filter chain.
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    // 1. Extract the JWT from the request header.
    String token = getJWTfromRequest(request); // parse Bearer token if present

    // 2. Validate the token.
    if (StringUtils.hasText(token)
        && tokenProvider.validateToken(
            token)) { // only proceed if structurally & cryptographically valid
      // 3. Get the username from the token.
      String username = tokenProvider.getUsername(token); // subject claim

      // 4. Load the user details from the database.
      UserDetails userDetails =
          userDetailsService.loadUserByUsername(
              username); // load authorities to populate security context

      // 5. Create an authentication token.
      UsernamePasswordAuthenticationToken authenticationToken =
          new UsernamePasswordAuthenticationToken(
              userDetails, // principal
              null, // no credentials stored post-auth
              userDetails.getAuthorities() // roles/authorities
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
