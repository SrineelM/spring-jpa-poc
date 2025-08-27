package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.Tags;

import java.util.concurrent.Executor;

/**
 * This configuration class defines a custom thread pool for handling asynchronous tasks.
 * Using a custom executor is a best practice for production applications, as it allows you to
 * fine-tune the thread pool settings to match your application's workload and prevents
 * resource contention with other parts of the framework.
 * 
 * Key features:
 * - Custom ThreadPoolTaskExecutor with configurable pool sizes and queue capacity
 * - Rejection policy with metrics tracking for monitoring saturation
 * - Graceful shutdown to allow in-flight tasks to complete
 * - Comprehensive metrics binding for observability
 */
@Configuration // Marks this class as a source of bean definitions
@EnableAsync // Enables Spring's asynchronous method execution capability, allowing @Async annotations to work
public class AsyncConfig {

    /**
     * Creates a custom ThreadPoolTaskExecutor bean named "taskExecutor".
     * This executor will be used for all methods annotated with @Async.
     * It includes metrics binding for monitoring thread pool performance.
     *
     * @param meterRegistry The Micrometer MeterRegistry for registering metrics
     * @return A configured ThreadPoolTaskExecutor with metrics enabled
     */
    @Bean(name = "taskExecutor") // Defines a Spring bean with the name "taskExecutor"
    public Executor taskExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core pool size: The number of threads to keep in the pool, even if they are idle.
        // This ensures a minimum number of threads are always available for quick task execution.
        executor.setCorePoolSize(5);
        // Max pool size: The maximum number of threads allowed in the pool.
        // Threads beyond core pool size are created only when the queue is full.
        executor.setMaxPoolSize(10);
        // Queue capacity: The capacity of the queue to hold tasks before they are executed.
        // If the queue is full and max pool size is reached, tasks are rejected.
        executor.setQueueCapacity(25);
        // Thread name prefix: A descriptive name for the threads created by this executor, 
        // which is very useful for logging and debugging.
        executor.setThreadNamePrefix("Async-Task-");
        
        // Rejection policy enhanced with metrics counter for visibility.
        // We wrap CallerRunsPolicy so callers experience backpressure and we emit a rejection metric.
        // This helps in monitoring when the executor is saturated.
        final Counter rejectionCounter = Counter.builder("async.executor.rejections")
            .description("Number of rejected async tasks")
            .tags("executor","taskExecutor","app","spring-jpa-poc")
            .register(meterRegistry);
        executor.setRejectedExecutionHandler((r, exec) -> {
            rejectionCounter.increment(); // Increment counter when a task is rejected
            new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, exec);
        });
        
        // Graceful shutdown: Ensures in-flight tasks are allowed to finish when the application stops.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30); // Wait up to 30 seconds for tasks to complete
        
        // Initialize the executor with the configured settings
        executor.initialize();

        // --- Metrics Binding (now implemented) ---
        // Common tags for all executor-related metrics
        Tags commonTags = Tags.of("executor","taskExecutor","app","spring-jpa-poc","poolType","threadPool");
        
        // Bind standard JVM executor metrics (e.g., active threads, completed tasks, etc.)
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "async.executor", commonTags);
        
        // Custom gauges for quick dashboards (ExecutorServiceMetrics already supplies many, these are illustrative):
        // Gauge for current queue size - useful for monitoring backlog
        Gauge.builder("async.executor.queue.size", () -> executor.getThreadPoolExecutor().getQueue().size())
            .description("Current size of the async executor task queue")
            .tags(commonTags)
            .register(meterRegistry);
        
        // Gauge for current pool size - shows how many threads are currently in the pool
        Gauge.builder("async.executor.pool.size", () -> executor.getThreadPoolExecutor().getPoolSize())
            .description("Current number of threads in the pool")
            .tags(commonTags)
            .register(meterRegistry);
        
        // Gauge for active threads - indicates current workload
        Gauge.builder("async.executor.active", () -> executor.getThreadPoolExecutor().getActiveCount())
            .description("Current number of active threads")
            .tags(commonTags)
            .register(meterRegistry);
        
        // Gauge for completed tasks - tracks total throughput
        Gauge.builder("async.executor.completed", () -> executor.getThreadPoolExecutor().getCompletedTaskCount())
            .description("Total number of completed tasks")
            .tags(commonTags)
            .register(meterRegistry);
        
        // (Alerting Guidance) Alert when active/core or queue.size/capacity exceed sustained thresholds or on any rejection.
        // This helps in proactive monitoring and scaling decisions.
        
        return executor;
    }
}
