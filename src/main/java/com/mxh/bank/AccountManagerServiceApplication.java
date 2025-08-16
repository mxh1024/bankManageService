package com.mxh.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.mxh.bank.repository")
public class AccountManagerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountManagerServiceApplication.class, args);
    }
}
