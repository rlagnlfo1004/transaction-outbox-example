package com.example.transactionoutbox.infrastructure.outbox;

import com.example.transactionoutbox.domain.outbox.OutboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, Long> {

    // 발행되지 않은 메시지 조회 (생성 시간 순서 보장)
    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'READY' ORDER BY o.createdAt ASC")
    List<OutboxMessage> findAllByStatusReady();

    // 하루 이상 지난 발행 완료 메시지 삭제용 조회
    @Query("SELECT o FROM OutboxMessage o WHERE o.status = 'PUBLISHED' AND o.publishedAt < :threshold")
    List<OutboxMessage> findOldPublishedMessages(@Param("threshold") LocalDateTime threshold);
}
