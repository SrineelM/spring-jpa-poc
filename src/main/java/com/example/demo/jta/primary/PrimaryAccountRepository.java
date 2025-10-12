package com.example.demo.jta.primary;

import com.example.demo.jta.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrimaryAccountRepository extends JpaRepository<Account, Long> {
    Account findByUser(String user);
}
