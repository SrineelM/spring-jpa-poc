package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.exception.CustomCheckedException;
import com.example.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service demonstrates various declarative transaction management features in Spring using the @Transactional annotation.
 */
@Service
public class TransactionalUserService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalUserService.class);

    private final UserRepository userRepository;
    private final TransactionalUserService self;

    // We inject a proxy of the service itself to ensure that transactional annotations are applied correctly when calling methods from within the same class.
    public TransactionalUserService(UserRepository userRepository, TransactionalUserService self) {
        this.userRepository = userRepository;
        this.self = self;
    }

    /**
     * Demonstrates the rollbackFor attribute.
     * By default, Spring only rolls back transactions for unchecked exceptions (RuntimeException).
     * Here, we configure it to also roll back for our custom CustomCheckedException.
     *
     * @param user The user to create.
     * @throws CustomCheckedException to trigger a rollback.
     */
    @Transactional(rollbackFor = CustomCheckedException.class)
    public void createUserAndRollbackOnCheckedException(User user) throws CustomCheckedException {
        logger.info("Creating user: {}, expecting rollback on CustomCheckedException", user.getEmail());
        userRepository.save(user);
        throw new CustomCheckedException("Simulating a checked exception to trigger rollback.");
    }

    /**
     * Demonstrates the readOnly attribute.
     * The readOnly flag is a hint to the persistence provider that this transaction will not
     * perform any write operations. This can enable performance optimizations at the
     * JDBC driver, database, or JPA provider level.
     */
    @Transactional(readOnly = true)
    public long countUsers() {
        logger.info("Counting users in a read-only transaction.");
        return userRepository.count();
    }

    /**
     * This method demonstrates propagation behavior by calling a method with a different propagation level.
     */
    @Transactional
    public void demonstratePropagation(User user1, User user2) {
        logger.info("Outer transaction started for demonstratePropagation.");
        userRepository.save(user1);
        try {
            // We call the method on the proxied `self` instance to ensure the @Transactional annotation on the called method is honored.
            self.createUserWithRequiresNew(user2);
        } catch (Exception e) {
            logger.error("Exception caught in outer transaction: {}. The outer transaction will still commit user1.", e.getMessage());
        }
        logger.info("Outer transaction finished for demonstratePropagation.");
    }

    /**
     * This method demonstrates the REQUIRES_NEW propagation level.
     * When this method is called, Spring suspends the current transaction (if one exists)
     * and starts a completely new, independent transaction.
     * This is useful when you need to ensure an operation commits or rolls back regardless of the outer transaction's outcome.
     *
     * @param user The user to create in a new transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUserWithRequiresNew(User user) {
        logger.info("Inner transaction (REQUIRES_NEW) started for user: {}", user.getEmail());
        userRepository.save(user);
        logger.info("Inner transaction (REQUIRES_NEW) finished for user: {}", user.getEmail());
        // Uncomment the line below to see how a failure in the inner transaction does not affect the outer transaction.
        // throw new RuntimeException("Simulating failure in inner transaction");
    }

    /**
     * This method demonstrates the isolation level attribute.
     * Isolation levels define how transactions are isolated from each other, preventing
     * concurrency issues like dirty reads, non-repeatable reads, and phantom reads.
     * SERIALIZABLE is the highest isolation level, providing the most protection but potentially reducing concurrency.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public long countUsersWithSerializableIsolation() {
        logger.info("Counting users with SERIALIZABLE isolation level.");
        // In a real scenario, you might perform other operations here that require
        // protection from concurrent modifications.
        return userRepository.count();
    }
}
