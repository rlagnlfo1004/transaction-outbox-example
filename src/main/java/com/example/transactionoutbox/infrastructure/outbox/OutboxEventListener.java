package com.example.transactionoutbox.infrastructure.outbox;

import com.example.transactionoutbox.domain.outbox.OutboxMessage;
import com.example.transactionoutbox.domain.project.event.ProjectApprovedEvent;
import com.example.transactionoutbox.domain.project.event.ProjectRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventListener {

    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

//    @Async
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
@EventListener
public void handelProjectApprovedEvent(ProjectApprovedEvent event) {
    try {
        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("eventListener start [transactionName = {}]", transactionName);

        String payload = objectMapper.writeValueAsString(event);

        OutboxMessage outboxMessage = OutboxMessage.create(
                "PROJECT_REQUEST",
                event.getProjectRequestId(),
                "PROJECT_APPROVED",
                payload
        );

        outboxMessageRepository.save(outboxMessage);
        log.info("Outbox message saved: {}", outboxMessage.getId());
    } catch (JacksonException e) {
        log.error("Failed to serialize event", e);
        throw new RuntimeException("Event serialization failed", e);
    }
}

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleProjectRejectedEvent(ProjectRejectedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage outboxMessage = OutboxMessage.create(
                    "PROJECT_APPLICATION",
                    event.getProjectRequestId(),
                    "PROJECT_REJECTED",
                    payload
            );

            outboxMessageRepository.save(outboxMessage);
            log.info("Outbox message saved: {}", outboxMessage.getId());
        } catch (JacksonException e) {
            log.error("Failed to serialize event", e);
            throw new RuntimeException("Event serialization failed", e);
        }
    }
}
