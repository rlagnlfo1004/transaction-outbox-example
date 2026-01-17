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

//    private final DiscordClient discordClient;
    private final ObjectMapper objectMapper;

    @Override
    public void send(String eventType, String payload) {
        try {
            switch (eventType) {
                case "PROJECT_APPROVED":
                    ProjectApprovedEvent approvedEvent =
                            objectMapper.readValue(payload, ProjectApprovedEvent.class);
//                    discordClient.sendMessage(...));
                    break;

                case "PROJECT_REJECTED":
                    ProjectRejectedEvent rejectedEvent =
                            objectMapper.readValue(payload, ProjectRejectedEvent.class);
//                    discordClient.sendMessage(buildRejectedMessage(rejectedEvent));
                    break;

                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to send Discord message", e);
            throw new RuntimeException("Discord message send failed", e);
        }
    }
}
