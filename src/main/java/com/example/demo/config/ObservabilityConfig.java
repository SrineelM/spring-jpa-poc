package com.example.demo.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for comprehensive observability and metrics using Micrometer.
 * This class enables and configures aspects for monitoring the application's performance.
 */
@Configuration
@EnableAspectJAutoProxy // Enables support for AspectJ-based aspects
public class ObservabilityConfig {

    /**
     * Customizes the MeterRegistry to add common tags to all metrics.
     * Common tags are useful for filtering and aggregating metrics in a monitoring system.
     *
     * @return A MeterRegistryCustomizer that adds an "application" tag.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        // This lambda customizes the MeterRegistry by adding a common tag.
        // The tag "application" with value "spring-jpa-poc" will be attached to every metric.
        return registry -> registry.config().commonTags("application", "spring-jpa-poc");
    }

    /**
     * Creates a TimedAspect bean to enable the @Timed annotation.
     * This aspect automatically records timing metrics for methods annotated with @Timed.
     *
     * @param registry The MeterRegistry to which the metrics are reported.
     * @return A configured TimedAspect.
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Creates a CountedAspect bean to enable the @Counted annotation.
     * This aspect automatically records count metrics for methods annotated with @Counted.
     *
     * @param registry The MeterRegistry to which the metrics are reported.
     * @return A configured CountedAspect.
     */
    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Creates a Timer.Sample bean for programmatic timing.
     * This allows for more fine-grained control over timing measurements within the code.
     *
     * @param meterRegistry The MeterRegistry to use for creating the timer.
     * @return A Timer.Sample instance to start a timing measurement.
     */
    @Bean
    public Timer.Sample customTimer(MeterRegistry meterRegistry) {
        // Timer.start() creates a sample that holds the start time and a reference to the registry.
        // The timing is stopped by calling sample.stop(timer).
        return Timer.start(meterRegistry);
    }
}
