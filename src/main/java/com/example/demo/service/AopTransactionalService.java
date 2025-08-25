package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service is used to demonstrate AOP-based transaction management.
 * Notice that this class and its methods do NOT have the @Transactional annotation.
 * The transactional behavior is applied by the AOP configuration defined in {@link com.example.demo.config.AopConfig}.
 */
@Service
public class AopTransactionalService {

    private static final Logger logger = LoggerFactory.getLogger(AopTransactionalService.class);

    private final UserRepository userRepository;

    public AopTransactionalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Creates a user. This method is expected to be executed within a transaction
     * that will be successfully committed, thanks to our AOP configuration.
     *
     * @param user The user to create.
     */
    public void createUserSuccessfully(User user) {
        logger.info("Executing createUserSuccessfully in AopTransactionalService for user: {}", user.getEmail());
        userRepository.save(user);
        logger.info("User {} saved successfully via AOP-based transaction.", user.getEmail());
    }

    /**
     * Attempts to create a user but throws an exception to trigger a rollback.
     * The rollback is handled by the AOP transactional advice.
     *
     * @param user The user to create.
     */
    public void createUserAndRollback(User user) {
        logger.info("Attempting to create user {} via AOP, expecting rollback.", user.getEmail());
        userRepository.save(user);
        logger.info("User {} saved, but now throwing exception to trigger AOP-based rollback.", user.getEmail());
        throw new RuntimeException("Simulating failure to trigger AOP-based rollback.");
    }
}
