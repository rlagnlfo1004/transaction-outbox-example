package com.example.transactionoutbox.application.project;

import com.example.transactionoutbox.domain.project.ProjectRequest;
import com.example.transactionoutbox.domain.project.event.ProjectApprovedEvent;
import com.example.transactionoutbox.domain.project.event.ProjectRejectedEvent;
import com.example.transactionoutbox.infrastructure.project.ProjectRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRequestRepository projectRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectRequest createProject(Long userId) {
        // 새로운 프로젝트 신청 생성
        ProjectRequest projectRequest = ProjectRequest.createNew(userId);
        return projectRequestRepository.save(projectRequest);
    }

    public void approveProject(Long projectRequestId) {
        ProjectRequest projectRequest = projectRequestRepository.findById(projectRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Project Request not found"));

        // 도메인 로직 실행
        projectRequest.approve();

        String transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("ProjectService [transactionName = {}]", transactionName);
        log.info("before event publish");

        // 도메인 이벤트 발행 (Spring Event)
        eventPublisher.publishEvent(
                new ProjectApprovedEvent(
                        projectRequest.getProjectRequestId(),
                        projectRequest.getUserId(),
                        LocalDateTime.now()
                )
        );
        transactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("ProjectService [transactionName = {}]", transactionName);
        log.info("complete event publish");
    }

    public void rejectProject(Long projectRequestId) {
        ProjectRequest projectRequest = projectRequestRepository.findById(projectRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Project Request not found"));

        // 도메인 로직 실행
        projectRequest.reject();

        // 도메인 이벤트 발행 (Spring Event)
        eventPublisher.publishEvent(
                new ProjectRejectedEvent(
                        projectRequest.getProjectRequestId(),
                        projectRequest.getUserId(),
                        LocalDateTime.now()
                )
        );
        log.info("complete event publish");
    }
}
