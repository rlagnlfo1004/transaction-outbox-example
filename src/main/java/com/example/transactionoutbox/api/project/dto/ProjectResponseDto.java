package com.example.transactionoutbox.api.project.dto;

import com.example.transactionoutbox.domain.project.ProjectRequest;
import com.example.transactionoutbox.domain.project.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ProjectResponseDto {
    private Long projectRequestId;
    private Long userId;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    public static ProjectResponseDto from(ProjectRequest projectRequest) {
        return new ProjectResponseDto(
                projectRequest.getProjectRequestId(),
                projectRequest.getUserId(),
                projectRequest.getStatus(),
                projectRequest.getCreatedAt(),
                projectRequest.getProcessedAt()
        );
    }
}

