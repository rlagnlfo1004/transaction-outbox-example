package com.example.transactionoutbox.infrastructure.message;

import com.example.transactionoutbox.domain.project.event.ProjectApprovedEvent;
import com.example.transactionoutbox.domain.project.event.ProjectRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordMessageSender implements MessageSender {

    private final ObjectMapper objectMapper;

    @Override
    public void send(String eventType, String payload) {
        try {
            switch (eventType) {
                case "PROJECT_APPROVED":
                    ProjectApprovedEvent approvedEvent =
                            objectMapper.readValue(payload, ProjectApprovedEvent.class);
                    log.info(buildApprovedMessage(approvedEvent)); // Discord 전송 로직 대체
                    break;

                case "PROJECT_REJECTED":
                    ProjectRejectedEvent rejectedEvent =
                            objectMapper.readValue(payload, ProjectRejectedEvent.class);
                    log.info(buildRejectedMessage(rejectedEvent)); // Discord 전송 로직 대체
                    break;

                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to send Discord message", e);
            throw new RuntimeException("Discord message send failed", e);
        }
    }

    private String buildApprovedMessage(ProjectApprovedEvent event) {
        return String.format("✅ 프로젝트 신청이 승인되었습니다. (신청 ID: %s)",
                event.getProjectRequestId());
    }

    private String buildRejectedMessage(ProjectRejectedEvent event) {
        return String.format("❌ 프로젝트 신청이 거절되었습니다. (신청 ID: %s)",
                event.getProjectRequestId());
    }
}
