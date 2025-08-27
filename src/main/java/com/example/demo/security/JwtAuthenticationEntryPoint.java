package com.example.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This class is an implementation of Spring Security's {@link AuthenticationEntryPoint}.
 * It is used to handle authentication failures for unauthenticated users who are
 * trying to access a protected resource.
 * <p>
 * When an unauthenticated user attempts to access a resource that requires
 * authentication, the {@link #commence} method is triggered, and it sends an
 * HTTP 401 Unauthorized response.
 */
@Component // plugged into HttpSecurity.exceptionHandling().authenticationEntryPoint(...)
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * This method is invoked when an unauthenticated user tries to access a protected resource.
     * It sends an HTTP 401 Unauthorized error response to the client.
     *
     * @param request       The request that resulted in an AuthenticationException.
     * @param response      The response, so that the user agent can begin authentication.
     * @param authException The exception that triggered the commencement.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        // Send an HTTP 401 Unauthorized error with the exception message.
        // This informs the client that authentication is required to access the resource.
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage()); // terminates filter chain with 401
    }
}
