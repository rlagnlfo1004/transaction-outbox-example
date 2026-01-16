package com.example.transactionoutbox.infrastructure.message;

public interface MessageSender {
    void send(String eventType, String payload);
}
