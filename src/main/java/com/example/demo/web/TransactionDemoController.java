package com.example.demo.web;

import com.example.demo.domain.User;
import com.example.demo.exception.CustomCheckedException;
import com.example.demo.service.AopTransactionalService;
import com.example.demo.service.ProgrammaticTransactionService;
import com.example.demo.service.TransactionalUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller provides endpoints to demonstrate various transaction management strategies.
 * Each endpoint triggers a specific transactional scenario, allowing you to observe the behavior
 * in the application logs and the H2 database console.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionDemoController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionDemoController.class);

    private final TransactionalUserService transactionalUserService;
    private final ProgrammaticTransactionService programmaticTransactionService;
    private final AopTransactionalService aopTransactionalService;

    public TransactionDemoController(TransactionalUserService transactionalUserService,
                                   ProgrammaticTransactionService programmaticTransactionService,
                                   AopTransactionalService aopTransactionalService) {
        this.transactionalUserService = transactionalUserService;
        this.programmaticTransactionService = programmaticTransactionService;
        this.aopTransactionalService = aopTransactionalService;
    }

    /**
     * Demonstrates declarative transaction rollback for a checked exception.
     * A new user is created, but a CustomCheckedException is thrown, which should trigger a rollback.
     * Check the logs and H2 console to verify that the user is not saved.
     */
    @GetMapping("/declarative-rollback")
    public ResponseEntity<String> demoDeclarativeRollback() {
        User user = new User();
        user.setEmail("declarative-rollback@example.com");
        user.setName("Declarative Rollback");
        user.setPassword("password");
        try {
            transactionalUserService.createUserAndRollbackOnCheckedException(user);
        } catch (CustomCheckedException e) {
            logger.error("Caught expected exception in controller: {}", e.getMessage());
            return ResponseEntity.ok("Demonstrated declarative rollback. Check logs for details. The user should NOT be in the database.");
        }
        return ResponseEntity.ok("This should not be reached.");
    }

    /**
     * Demonstrates REQUIRES_NEW propagation.
     * The outer transaction saves user1. The inner transaction, running in a new transaction, saves user2.
     * Even if the inner transaction were to fail, the outer one would still commit.
     * Check the H2 console to verify that both users are saved.
     */
    @GetMapping("/declarative-propagation")
    public ResponseEntity<String> demoDeclarativePropagation() {
        User user1 = new User();
        user1.setEmail("propagation-outer@example.com");
        user1.setName("Propagation Outer");
        user1.setPassword("password");

        User user2 = new User();
        user2.setEmail("propagation-inner@example.com");
        user2.setName("Propagation Inner");
        user2.setPassword("password");

        transactionalUserService.demonstratePropagation(user1, user2);
        return ResponseEntity.ok("Demonstrated REQUIRES_NEW propagation. Both users should be in the database.");
    }

    /**
     * Demonstrates a successful programmatic transaction.
     * Check the H2 console to verify the user is saved.
     */
    @GetMapping("/programmatic-commit")
    public ResponseEntity<String> demoProgrammaticCommit() {
        User user = new User();
        user.setEmail("programmatic-commit@example.com");
        user.setName("Programmatic Commit");
        user.setPassword("password");
        programmaticTransactionService.createUserSuccessfully(user);
        return ResponseEntity.ok("Demonstrated programmatic commit. The user should be in the database.");
    }

    /**
     * Demonstrates a programmatic transaction that is rolled back.
     * Check the H2 console to verify the user is NOT saved.
     */
    @GetMapping("/programmatic-rollback")
    public ResponseEntity<String> demoProgrammaticRollback() {
        User user = new User();
        user.setEmail("programmatic-rollback@example.com");
        user.setName("Programmatic Rollback");
        user.setPassword("password");
        programmaticTransactionService.createUserAndRollback(user);
        return ResponseEntity.ok("Demonstrated programmatic rollback. The user should NOT be in the database.");
    }

    /**
     * Demonstrates a successful transaction managed by AOP.
     * Check the H2 console to verify the user is saved.
     */
    @GetMapping("/aop-commit")
    public ResponseEntity<String> demoAopCommit() {
        User user = new User();
        user.setEmail("aop-commit@example.com");
        user.setName("AOP Commit");
        user.setPassword("password");
        aopTransactionalService.createUserSuccessfully(user);
        return ResponseEntity.ok("Demonstrated AOP-based commit. The user should be in the database.");
    }

    /**
     * Demonstrates an AOP-managed transaction that is rolled back.
     * Check the H2 console to verify the user is NOT saved.
     */
    @GetMapping("/aop-rollback")
    public ResponseEntity<String> demoAopRollback() {
        User user = new User();
        user.setEmail("aop-rollback@example.com");
        user.setName("AOP Rollback");
        user.setPassword("password");
        try {
            aopTransactionalService.createUserAndRollback(user);
        } catch (Exception e) {
            logger.error("Caught expected exception from AOP-managed method: {}", e.getMessage());
        }
        return ResponseEntity.ok("Demonstrated AOP-based rollback. The user should NOT be in the database.");
    }
}
