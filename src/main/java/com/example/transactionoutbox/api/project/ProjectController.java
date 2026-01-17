package com.example.transactionoutbox.api.project;

import com.example.transactionoutbox.api.project.dto.ProjectRequestDto;
import com.example.transactionoutbox.api.project.dto.ProjectResponseDto;
import com.example.transactionoutbox.application.project.ProjectService;
import com.example.transactionoutbox.domain.project.ProjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

    private final ProjectService projectService;

    // 프로젝트 신청
    @PostMapping
    public ResponseEntity<ProjectResponseDto> createProject(@RequestBody ProjectRequestDto requestDto) {
        log.info("Creating project request for user: {}", requestDto.getUserId());
        ProjectRequest projectRequest = projectService.createProject(requestDto.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectResponseDto.from(projectRequest));
    }

    // 프로젝트 승인
    @PostMapping("/{projectRequestId}/approve")
    public ResponseEntity<Void> approveProject(@PathVariable Long projectRequestId) {
        log.info("Approving project request: {}", projectRequestId);
        projectService.approveProject(projectRequestId);
        return ResponseEntity.ok().build();
    }

    // 프로젝트 반려
    @PostMapping("/{projectRequestId}/reject")
    public ResponseEntity<Void> rejectProject(@PathVariable Long projectRequestId) {
        log.info("Rejecting project request: {}", projectRequestId);
        projectService.rejectProject(projectRequestId);
        return ResponseEntity.ok().build();
    }
}
