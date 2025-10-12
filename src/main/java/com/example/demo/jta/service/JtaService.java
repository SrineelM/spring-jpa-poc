package com.example.demo.jta.service;

import com.example.demo.jta.Account;
import com.example.demo.jta.primary.PrimaryAccountRepository;
import com.example.demo.jta.secondary.SecondaryAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer demonstrating a distributed transaction across two separate databases.
 */
@Service
public class JtaService {

    // Repository for the primary database.
    private final PrimaryAccountRepository primaryAccountRepository;
    // Repository for the secondary database.
    private final SecondaryAccountRepository secondaryAccountRepository;

    public JtaService(PrimaryAccountRepository primaryAccountRepository, SecondaryAccountRepository secondaryAccountRepository) {
        this.primaryAccountRepository = primaryAccountRepository;
        this.secondaryAccountRepository = secondaryAccountRepository;
    }

    /**
     * Transfers a specified amount from a user in the primary database to a user in the secondary database.
     * The @Transactional annotation ensures that this entire method executes as a single, atomic JTA transaction.
     * If any part of the operation fails (e.g., an exception is thrown), both the withdrawal from the primary
     * account and the deposit into the secondary account will be rolled back.
     *
     * @param fromUser The username in the primary database to transfer funds from.
     * @param toUser The username in the secondary database to transfer funds to.
     * @param amount The amount to transfer.
     */
    @Transactional
    public void transfer(String fromUser, String toUser, double amount) {
        // Retrieve the 'from' account from the primary database.
        Account fromAccount = primaryAccountRepository.findByUser(fromUser);

        // Retrieve the 'to' account from the secondary database.
        Account toAccount = secondaryAccountRepository.findByUser(toUser);

        // It's good practice to check if accounts exist and have sufficient balance.
        // For this demo, we assume they exist and the balance is sufficient.

        // Perform the withdrawal from the primary account.
        fromAccount.setBalance(fromAccount.getBalance() - amount);

        // Perform the deposit into the secondary account.
        toAccount.setBalance(toAccount.getBalance() + amount);

        // Save the updated account in the primary database.
        primaryAccountRepository.save(fromAccount);

        // To simulate a failure, one could throw an exception here.
        // For example: if (true) { throw new RuntimeException("Simulating a failure during transfer!"); }
        // If this exception were thrown, both the save operations above would be rolled back by the JTA transaction manager.

        // Save the updated account in the secondary database.
        secondaryAccountRepository.save(toAccount);
    }
}
