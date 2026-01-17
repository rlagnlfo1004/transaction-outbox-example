package com.example.transactionoutbox.domain.project.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectApprovedEvent {
    private Long projectRequestId;
    private Long userId;
    private LocalDateTime approvedAt;
}
