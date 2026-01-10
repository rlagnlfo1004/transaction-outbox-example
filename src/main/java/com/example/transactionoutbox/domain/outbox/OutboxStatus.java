package com.example.transactionoutbox.domain.outbox;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum OutboxStatus {
    READY,      // 발행 대기
    PUBLISHED   // 발행 완료
}
