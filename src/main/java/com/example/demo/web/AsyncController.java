package com.example.demo.web;

import com.example.demo.service.AsyncService;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller exposes an endpoint to demonstrate asynchronous processing. It allows clients to
 * trigger long-running tasks without blocking the main request thread.
 */
@RestController
@RequestMapping("/api/async")
public class AsyncController {

    private final AsyncService asyncService;

    public AsyncController(AsyncService asyncService) {
        this.asyncService = asyncService;
    }

    /**
     * This endpoint triggers a long-running task in the {@link AsyncService}. The task is executed in
     * a background thread, and the client receives an immediate response with a {@link
     * CompletableFuture}.
     *
     * @return A {@link CompletableFuture} that will be completed with the result of the task.
     */
    @GetMapping("/long-task")
    public CompletableFuture<String> performLongRunningTask() {
        return asyncService.performLongRunningTask();
    }
}
