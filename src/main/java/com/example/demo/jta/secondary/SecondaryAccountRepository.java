package com.example.demo.jta.secondary;

import com.example.demo.jta.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecondaryAccountRepository extends JpaRepository<Account, Long> {
    Account findByUser(String user);
}
