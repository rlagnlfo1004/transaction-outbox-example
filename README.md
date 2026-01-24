# Transaction Outbox Pattern 구현

이 프로젝트는 Spring Boot를 사용하여 Transaction Outbox Pattern을 구현한 예제입니다.

## 프로젝트 개요

프로젝트 신청/승인/거절 시스템에서 외부 메시징 시스템(Discord)으로 안정적으로 이벤트를 전달하기 위해 Transactional Outbox Pattern을 구현했습니다.

### 주요 기능

1. **프로젝트 신청**: 사용자가 프로젝트를 신청합니다.
2. **프로젝트 승인**: 관리자가 프로젝트를 승인하면 이벤트가 발생합니다.
3. **프로젝트 거절**: 관리자가 프로젝트를 거절하면 이벤트가 발생합니다.
4. **Outbox Pattern**: 이벤트는 즉시 외부로 전송되지 않고 Outbox 테이블에 저장됩니다.
5. **비동기 폴링**: 스케줄러가 주기적으로 Outbox 메시지를 조회하여 외부 시스템으로 전송합니다.

## 기술 스택

- Java 21
- Spring Boot 4.0.1
- Spring Data JPA
- MySQL
- Lombok
- Jackson

## 아키텍처

### Outbox Pattern 흐름

1. 사용자가 프로젝트 승인/거절 요청
2. 트랜잭션 내에서:
   - 프로젝트 상태 업데이트 (PENDING → APPROVED/REJECTED)
   - 도메인 이벤트 발행 (Spring Application Event)
   - 이벤트 리스너가 Outbox 테이블에 메시지 저장
3. 트랜잭션 커밋
4. 별도의 스케줄러가 5초마다 폴링:
   - Outbox 테이블에서 READY 상태 메시지 조회
   - Discord로 메시지 전송
   - 성공시 메시지 상태를 PUBLISHED로 변경
5. 매일 새벽 2시에 오래된 발행 완료 메시지 정리

## 환경 설정

### 필수 환경변수

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/outbox_db?serverTimezone=Asia/Seoul
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
```

### MySQL 데이터베이스 생성

```sql
CREATE DATABASE outbox_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

테이블은 JPA의 `ddl-auto: update` 설정으로 자동 생성됩니다.

## 빌드 및 실행

### 빌드

```bash
./gradlew clean build
```

### 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/transaction-outbox-0.0.1-SNAPSHOT.jar
```

## API 엔드포인트

### 1. 프로젝트 신청

```bash
curl -X POST http://localhost:8080/api/v1/projects \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1
  }'
```

**응답:**
```json
{
  "projectRequestId": 1,
  "userId": 1,
  "status": "PENDING",
  "createdAt": "2025-01-17T10:30:00",
  "processedAt": null
}
```

### 2. 프로젝트 승인

```bash
curl -X POST http://localhost:8080/api/v1/projects/1/approve
```

**응답:** 200 OK

이 작업 후:
- 프로젝트 상태가 `APPROVED`로 변경됨
- Outbox 테이블에 `PROJECT_APPROVED` 메시지 저장됨
- 스케줄러가 메시지를 감지하여 Discord로 전송

### 3. 프로젝트 거절

```bash
curl -X POST http://localhost:8080/api/v1/projects/1/reject
```

**응답:** 200 OK

이 작업 후:
- 프로젝트 상태가 `REJECTED`로 변경됨
- Outbox 테이블에 `PROJECT_REJECTED` 메시지 저장됨
- 스케줄러가 메시지를 감지하여 Discord로 전송

## 데이터베이스 스키마

### project_request 테이블

| 컬럼명              | 타입           | 설명                   |
|-------------------|--------------|----------------------|
| project_request_id | BIGINT       | PK, 자동 증가           |
| user_id           | BIGINT       | 신청한 사용자 ID          |
| status            | VARCHAR(50)  | PENDING/APPROVED/REJECTED |
| created_at        | DATETIME     | 신청 생성 시간            |
| processed_at      | DATETIME     | 승인/거절 처리 시간         |

### outbox_messages 테이블

| 컬럼명            | 타입          | 설명                      |
|----------------|-------------|-------------------------|
| id             | BIGINT      | PK, 자동 증가              |
| aggregate_type | VARCHAR(100)| 도메인 엔티티 타입 (PROJECT_REQUEST) |
| aggregate_id   | BIGINT      | 관련 엔티티 ID              |
| event_type     | VARCHAR(100)| 이벤트 타입 (PROJECT_APPROVED) |
| payload        | TEXT        | 이벤트 데이터 (JSON)         |
| status         | VARCHAR(50) | READY/PUBLISHED         |
| created_at     | DATETIME    | 메시지 생성 시간             |
| published_at   | DATETIME    | 메시지 발행 시간             |

## 테스트 시나리오

### 시나리오 1: 정상 승인 플로우

1. 프로젝트 신청 생성
   ```bash
   curl -X POST http://localhost:8080/api/v1/projects \
     -H "Content-Type: application/json" \
     -d '{"userId": 100}'
   ```

2. 신청 ID 확인 (예: 1)

3. 프로젝트 승인
   ```bash
   curl -X POST http://localhost:8080/api/v1/projects/1/approve
   ```

4. 로그 확인
   - Outbox 메시지 저장 확인
   - 5초 이내에 폴링 작업 실행 확인
   - Discord 메시지 전송 로그 확인

### 시나리오 2: 거절 플로우

1. 새 프로젝트 신청 생성
2. 프로젝트 거절
   ```bash
   curl -X POST http://localhost:8080/api/v1/projects/2/reject
   ```

### 시나리오 3: 실패 재시도

메시지 전송 실패시 Outbox 메시지는 READY 상태로 유지되어 다음 폴링때 자동으로 재시도됩니다.

## 설정 커스터마이징

`application.yml`에서 다음 설정을 조정할 수 있습니다:

```yaml
spring:
  outbox:
    polling:
      interval: 5000  # 폴링 간격 (밀리초)
    cleanup:
      cron: "0 0 2 * * ?"  # 메시지 정리 스케줄 (Cron 표현식)
```

## 프로젝트 구조

```
src/main/java/com/example/transactionoutbox/
├── api/                          # REST API 레이어
│   └── project/
│       ├── ProjectController.java
│       └── dto/
│           ├── ProjectRequestDto.java
│           └── ProjectResponseDto.java
├── application/                  # 응용 서비스 레이어
│   └── project/
│       └── ProjectService.java
├── domain/                       # 도메인 레이어
│   ├── outbox/
│   │   ├── OutboxMessage.java
│   │   └── OutboxStatus.java
│   └── project/
│       ├── ProjectRequest.java
│       ├── RequestStatus.java
│       └── event/
│           ├── ProjectApprovedEvent.java
│           └── ProjectRejectedEvent.java
├── infrastructure/               # 인프라 레이어
│   ├── message/
│   │   ├── MessageSender.java
│   │   └── DiscordMessageSender.java
│   ├── outbox/
│   │   ├── OutboxEventListener.java
│   │   ├── OutboxMessagePublisher.java
│   │   └── OutboxMessageRepository.java
│   └── project/
│       └── ProjectRequestRepository.java
├── config/
│   └── JacksonConfig.java
└── TransactionOutboxApplication.java
```

## 장점

1. **데이터 일관성**: 도메인 업데이트와 메시지 발행이 같은 트랜잭션에서 처리
2. **메시지 유실 방지**: 메시지가 DB에 저장되므로 유실되지 않음
3. **재시도 메커니즘**: 전송 실패시 자동으로 재시도
4. **순서 보장**: 생성 시간 순서로 메시지 처리
5. **확장성**: 폴링 스케줄러를 별도 인스턴스로 분리 가능

## 로그 확인

애플리케이션 실행 중 다음과 같은 로그를 확인할 수 있습니다:

```
INFO  c.e.t.a.p.ProjectController : Creating project request for user: 100
INFO  c.e.t.i.o.OutboxEventListener : Outbox message saved: 1
INFO  c.e.t.i.o.OutboxMessagePublisher : Found 1 messages to publish
INFO  c.e.t.i.m.DiscordMessageSender : ✅ 프로젝트 신청이 승인되었습니다. (신청 ID: 1)
INFO  c.e.t.i.o.OutboxMessagePublisher : Message published successfully: id=1, eventType=PROJECT_APPROVED
```
