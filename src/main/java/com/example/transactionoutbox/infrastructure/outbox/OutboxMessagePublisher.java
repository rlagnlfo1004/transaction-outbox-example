package com.example.transactionoutbox.infrastructure.outbox;

import com.example.transactionoutbox.domain.outbox.OutboxMessage;
import com.example.transactionoutbox.infrastructure.message.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxMessagePublisher {

    private final OutboxMessageRepository outboxMessageRepository;
    private final MessageSender messageSender; // Discord, Email 전송 인터페이스

    @Scheduled(fixedDelayString = "${outbox.polling.interval:5000}") // 5초 마다
    @Transactional
    public void publishPendingMessages() {
        List<OutboxMessage> readyMessages = outboxMessageRepository.findAllByStatusReady();

        if (readyMessages.isEmpty()) {
            return;
        }

        log.info("Found {} messages to publish", readyMessages.size());

        for (OutboxMessage message : readyMessages) {
            try {
                // 실제 메시지 발행 (Discord, Email, Kafka 등)
                messageSender.send(
                        message.getEventType(),
                        message.getPayload()
                );

                // 발행 성공시 상태 변경
                message.markAsPublished();
                log.info("Message published successfully: id={}, eventType={}",
                        message.getId(), message.getEventType());
            } catch (Exception e) {
                log.error("Failed to publish message: id={}, eventType={}",
                        message.getId(), message.getEventType(), e);
                // 실패한 메시지는 status가 READY 그대로 유지되어 다음 폴링때 재시도
            }
        }
    }

    @Scheduled(cron = "${outbox.cleanup.cron:0 0 2 * * ?}") // 매일 새벽 2시
    @Transactional
    public void cleanupPublishedMessages() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(1);
        List<OutboxMessage> oldMessages = outboxMessageRepository.findOldPublishedMessages(threshold);

        if (!oldMessages.isEmpty()) {
            outboxMessageRepository.deleteAll(oldMessages);
            log.info("cleaned up {} old published messages", oldMessages.size());
        }
    }
}
