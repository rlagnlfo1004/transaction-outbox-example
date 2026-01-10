package com.example.transactionoutbox.infrastructure.project;

import com.example.transactionoutbox.domain.project.ProjectRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRequestRepository extends JpaRepository<ProjectRequest, Long> {
}

