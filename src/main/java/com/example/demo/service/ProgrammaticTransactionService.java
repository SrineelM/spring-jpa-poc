package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * This service demonstrates programmatic transaction management using Spring's TransactionTemplate.
 * This approach is useful when you need more fine-grained control over transactions than what declarative
 * annotations (@Transactional) can provide.
 */
@Service
public class ProgrammaticTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(ProgrammaticTransactionService.class);

    private final TransactionTemplate transactionTemplate;
    private final UserRepository userRepository;

    public ProgrammaticTransactionService(TransactionTemplate transactionTemplate, UserRepository userRepository) {
        this.transactionTemplate = transactionTemplate;
        this.userRepository = userRepository;
    }

    /**
     * Creates a user within a programmatic transaction that is expected to succeed.
     *
     * @param user The user to create.
     */
    public void createUserSuccessfully(User user) {
        // The execute method runs the provided code within a transaction.
        // If the callback completes without an exception, the transaction is committed.
        transactionTemplate.execute(status -> {
            logger.info("Executing successful programmatic transaction for user: {}", user.getEmail());
            userRepository.save(user);
            logger.info("User {} saved successfully within programmatic transaction.", user.getEmail());
            return null; // Return value can be used if needed
        });
    }

    /**
     * Attempts to create a user within a programmatic transaction that will be rolled back.
     *
     * @param user The user to create.
     */
    public void createUserAndRollback(User user) {
        logger.info("Attempting to create user {} in a transaction that will be rolled back.", user.getEmail());
        try {
            transactionTemplate.execute(status -> {
                logger.info("Executing programmatic transaction for user: {}, expecting rollback.", user.getEmail());
                userRepository.save(user);
                logger.info("User {} saved, but now throwing exception to trigger rollback.", user.getEmail());
                // By throwing a RuntimeException, we signal to the TransactionTemplate that the transaction should be rolled back.
                throw new RuntimeException("Simulating failure to trigger programmatic rollback.");
            });
        } catch (Exception e) {
            logger.error("Caught expected exception after programmatic rollback: {}", e.getMessage());
        }
    }
}
