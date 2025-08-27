package com.example.demo.service;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * This service demonstrates how to perform asynchronous operations in Spring. The {@link Async}
 * annotation enables methods to be executed in a separate thread, allowing the caller to proceed
 * without waiting for the task to complete.
 */
@Service
public class AsyncService {

  private static final Logger logger = LoggerFactory.getLogger(AsyncService.class);

  /**
   * Performs a simulated long-running task asynchronously.
   *
   * <p>The {@link Async} annotation tells Spring to run this method in a background thread pool.
   * This is useful for operations that might take a long time, such as processing a large file,
   * sending an email, or calling a slow external service.
   *
   * <p>The method returns a {@link CompletableFuture}, which is a handle to the result of the
   * asynchronous computation. The client can use this to get the result when it is available.
   *
   * @return A {@link CompletableFuture} that will be completed with the result of the task.
   */
  @Async
  public CompletableFuture<String> performLongRunningTask() {
    logger.info("Starting long-running task in a background thread...");
    try {
      // Simulate a task that takes 5 seconds to complete.
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      logger.error("The asynchronous task was interrupted.", e);
      // Preserve the interrupted status
      Thread.currentThread().interrupt();
    }
    logger.info("Long-running task has finished.");

    // Once the task is complete, the CompletableFuture is completed with the result.
    return CompletableFuture.completedFuture(
        "Task completed successfully!"); // immediately resolved future
  }
}
