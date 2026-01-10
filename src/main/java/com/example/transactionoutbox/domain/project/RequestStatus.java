package com.example.transactionoutbox.domain.project;

public enum RequestStatus {
    PENDING,   // 승인 대기
    APPROVED,  // 승인됨
    REJECTED   // 거절됨
}
