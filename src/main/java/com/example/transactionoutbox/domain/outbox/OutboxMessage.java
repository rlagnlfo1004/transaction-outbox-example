package com.example.transactionoutbox.domain.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String aggregateType;  // 어떤 도메인 엔티티 관련인지 (예: "PROJECT_APPLICATION")

    @Column(nullable = false)
    private String aggregateId;    // 해당 엔티티의 ID

    @Column(nullable = false, length = 100)
    private String eventType;      // 이벤트 타입 (예: "PROJECT_APPROVED")

    @Column(nullable = false)
    private String payload;        // 실제 이벤트 데이터 (JSON)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;   // READY, PUBLISHED

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.status = OutboxStatus.READY;
    }

    public static OutboxMessage create(String aggregateType, String aggregateId, String eventType, String payload) {
        OutboxMessage message = new OutboxMessage();
        message.aggregateType = aggregateType;
        message.aggregateId = aggregateId;
        message.eventType = eventType;
        message.payload = payload;
        return message;
    }

    // 발행 완료로 상태 변경
    public void markAsPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
}

