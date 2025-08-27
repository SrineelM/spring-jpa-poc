package com.example.demo.config;

import com.example.demo.interceptor.RateLimitInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for rate limiting. This class implements WebMvcConfigurer to register custom
 * interceptors. It is used to add the RateLimitInterceptor to the application's request processing
 * pipeline.
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

  private final RateLimitInterceptor rateLimitInterceptor;

  /**
   * Constructor for dependency injection. Spring will inject the RateLimitInterceptor bean.
   *
   * @param rateLimitInterceptor The interceptor to be registered.
   */
  public RateLimitConfig(RateLimitInterceptor rateLimitInterceptor) {
    this.rateLimitInterceptor = rateLimitInterceptor;
  }

  /**
   * Registers the RateLimitInterceptor. This method adds the interceptor to the
   * InterceptorRegistry, and specifies that it should apply to all paths under "/api/".
   *
   * @param registry The InterceptorRegistry to add the interceptor to.
   */
  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/api/**");
  }
}
