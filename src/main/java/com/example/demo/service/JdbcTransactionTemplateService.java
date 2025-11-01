package com.example.demo.service;

import com.example.demo.dto.UserSummaryDto;
import com.example.demo.repository.jdbc.UserJdbcRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Programmatic transactions using TransactionTemplate with Spring JDBC operations.
 * Keeps it small and focused for the POC.
 */
@Service
public class JdbcTransactionTemplateService {

    private static final Logger log = LoggerFactory.getLogger(JdbcTransactionTemplateService.class);

    private final TransactionTemplate tx;
    private final UserJdbcRepository jdbcRepo;

    public JdbcTransactionTemplateService(TransactionTemplate tx, UserJdbcRepository jdbcRepo) {
        this.tx = tx;
        this.jdbcRepo = jdbcRepo;
    }

    /**
     * Batch update user names atomically; if an exception is thrown inside the callback, all updates roll back.
     */
    public int[] updateNamesAtomically(Map<Long, String> updates) {
        return tx.execute(status -> {
            log.info("Updating {} user names in a single JDBC transaction", updates.size());
            int[] result = jdbcRepo.batchUpdateNames(updates);
            return result;
        });
    }

    /**
     * Read-only example wrapping a simple DTO projection inside a transaction.
     */
    public List<UserSummaryDto> readSummaries() {
        return tx.execute(status -> jdbcRepo.findSummaries());
    }
}
