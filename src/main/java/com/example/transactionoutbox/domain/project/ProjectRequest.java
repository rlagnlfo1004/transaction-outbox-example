package com.example.transactionoutbox.domain.project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectRequestId;

    @Column(nullable = false)
    private Long userId;     // 누가 신청했는지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;  // PENDING, APPROVED, REJECTED

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;  // 승인/거절 처리 시간

    public void approve() {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 승인할 수 있습니다.");
        }
        this.status = RequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status != RequestStatus.PENDING) {
            throw new IllegalStateException("대기 중인 신청만 거절할 수 있습니다.");
        }
        this.status = RequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public static ProjectRequest createNew(Long userId) {
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.userId = userId;
        projectRequest.status = RequestStatus.PENDING;
        projectRequest.createdAt = LocalDateTime.now();
        return projectRequest;
    }
}
