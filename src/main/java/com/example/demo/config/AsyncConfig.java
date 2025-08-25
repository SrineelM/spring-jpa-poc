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
 */
@Configuration
@EnableAsync // This annotation enables Spring's asynchronous method execution capability.
public class AsyncConfig {

    /**
     * Creates a custom ThreadPoolTaskExecutor bean named "taskExecutor".
     * This executor will be used for all methods annotated with @Async.
     *
     * @return A configured ThreadPoolTaskExecutor.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor(MeterRegistry meterRegistry) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // The number of threads to keep in the pool, even if they are idle.
        executor.setCorePoolSize(5);
        // The maximum number of threads allowed in the pool.
        executor.setMaxPoolSize(10);
        // The capacity of the queue to hold tasks before they are executed.
        executor.setQueueCapacity(25);
        // A descriptive name for the threads created by this executor, which is very useful for logging and debugging.
        executor.setThreadNamePrefix("Async-Task-");
    // Rejection policy enhanced with metrics counter for visibility.
    // We wrap CallerRunsPolicy so callers experience backpressure and we emit a rejection metric.
    final Counter rejectionCounter = Counter.builder("async.executor.rejections")
        .description("Number of rejected async tasks")
        .tags("executor","taskExecutor","app","spring-jpa-poc")
        .register(meterRegistry);
    executor.setRejectedExecutionHandler((r, exec) -> {
        rejectionCounter.increment();
        new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy().rejectedExecution(r, exec);
    });
    // Graceful shutdown so in‑flight tasks are allowed to finish when the application stops.
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30); // POC value; tune per p99 task duration in prod.
    // NOTE (Prod): Expose executor metrics (active, queue size, completed) via Micrometer.
    // Option 1: Inject MeterBinder; Option 2: Use ExecutorServiceMetrics.monitor(meterRegistry, executor, "async.executor").
    // This is omitted here to keep the POC lightweight.
    // NOTE (Scaling): Externalize these numbers to configuration (application.yml) or a dynamic source (e.g. Spring Cloud Config)
    // so you can right-size without redeploying.
        executor.initialize();

    // --- Metrics Binding (now implemented) ---
    Tags commonTags = Tags.of("executor","taskExecutor","app","spring-jpa-poc","poolType","threadPool");
    ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "async.executor", commonTags);
    // Custom gauges for quick dashboards (ExecutorServiceMetrics already supplies many, these are illustrative):
    Gauge.builder("async.executor.queue.size", () -> executor.getThreadPoolExecutor().getQueue().size())
        .description("Current size of the async executor task queue")
        .tags(commonTags)
        .register(meterRegistry);
    Gauge.builder("async.executor.pool.size", () -> executor.getThreadPoolExecutor().getPoolSize())
        .description("Current number of threads in the pool")
        .tags(commonTags)
        .register(meterRegistry);
    Gauge.builder("async.executor.active", () -> executor.getThreadPoolExecutor().getActiveCount())
        .description("Current number of active threads")
        .tags(commonTags)
        .register(meterRegistry);
    Gauge.builder("async.executor.completed", () -> executor.getThreadPoolExecutor().getCompletedTaskCount())
        .description("Total number of completed tasks")
        .tags(commonTags)
        .register(meterRegistry);
    // (Alerting Guidance) Alert when active/core or queue.size/capacity exceed sustained thresholds or on any rejection.
    // ------------------
        return executor;
    }
}
