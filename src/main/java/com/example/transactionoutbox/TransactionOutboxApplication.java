package com.example.transactionoutbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TransactionOutboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionOutboxApplication.class, args);
    }

}
