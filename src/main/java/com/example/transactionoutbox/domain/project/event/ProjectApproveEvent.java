package com.example.transactionoutbox.domain.project.event;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ProjectApproveEvent {
    private Long projectRequestId;
    private Long userId;
    private LocalDateTime approvedAt;
}
