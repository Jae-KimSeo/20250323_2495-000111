# 알림 프론트 서버

## 지원자 정보
- **수험번호**: 2495-000111
- **이름**: 김재현
- **지원공고명**: 서버 개발자 - 대고객 서비스

## 프로젝트 설명

알림 프론트 서버는 다양한 채널(SMS, 카카오톡, 이메일)을 통해 알림을 관리하고 처리하는 애플리케이션입니다. 알림 등록 및 조회를 위한 API를 제공하며, 비동기 처리, 스케줄링, 오류 복구와 같은 복잡한 요구사항을 처리합니다.

### 디렉토리 구조

프로젝트 구조입니다. 헥사고날 아키텍처(포트 및 어댑터) 패턴을 따릅니다:

```
src/main/java/org/service/alarmfront/
├── adapter              # 외부 상호작용을 위한 어댑터
│   ├── in               # 입력 어댑터 (컨트롤러 등)
│   │   └── web          # REST 컨트롤러가 있는 웹 레이어
│   └── out              # 출력 어댑터
│       ├── api          # 외부 API 통합
│       └── persistence  # 데이터베이스 영속성
├── application          # 애플리케이션 레이어
│   ├── port             # 입/출력 경계를 정의하는 포트
│   │   ├── in           # 입력 포트 (유스케이스)
│   │   └── out          # 출력 포트
│   └── service          # 유스케이스를 구현하는 서비스
├── batch                # 배치 처리 구성
├── config               # 구성 클래스
└── domain               # 도메인 모델 (핵심 비즈니스 로직)
    ├── entity           # 도메인 엔티티
    ├── exception        # 도메인별 예외
    └── value            # 값 객체 및 열거형
```

### 구현 상의 특징

시스템은 요구사항을 충족하기 위해 다음의 사항들을 고려하였습니다.:

1. **확장성을 위한 전략 패턴**:
   - 다양한 클라이언트 유형(모바일, 웹, 시스템)을 전용 strategy pattern 으로 처리
   - 공통 기능을 공유하기 위한 추상 템플릿 패턴 사용
   - 새로운 클라이언트 유형이나 알림 채널을 쉽게 확장 가능

2. **비동기 처리**:
   - 전용 워커 스레드를 통한 논블로킹 요청 처리
   - 알림 처리를 관리하는 인메모리 큐 시스템
   - 요청 처리와 실제 처리를 분리하여 빠른 API 응답 시간 보장

3. **데이터베이스 인덱싱**:
   - 자주 조회되는 컬럼에 인덱스 적용:
     ```java
     @Table(name = "notification_requests", indexes = {
         @Index(name = "idx_notification_req_created", columnList = "created_at"),
         @Index(name = "idx_notification_req_status", columnList = "status"),
         @Index(name = "idx_notification_req_target_created", columnList = "target_id, created_at")
     })
     ```
   - 고객 ID별 이력 조회 기능
   - 효율적인 배치 처리 작업

4. **트랜잭션 관리**:
   - 데이터 일관성을 위한 명확한 트랜잭션 경계
   - 트랜잭션 롤백을 포함한 적절한 오류 처리
   - 읽기 및 쓰기 작업 분리

5. **배치 처리**:
   - 정기적인 간격으로 예약된 알림 처리
   - 구성 가능한 재시도 횟수를 가진 실패한 알림 재시도 메커니즘
   - 스케줄링된 작업을 통한 자동 데이터 정리(3개월 이상):
     ```java
     @Scheduled(cron = "0 0 1 * * ?")
     @Transactional
     public void cleanupOldNotifications() {
         LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
         notificationRequestRepository.deleteNotificationsOlderThan(threeMonthsAgo);
     }
     ```

6. **resilience**:
   - 실패한 알림에 대한 재시도 메커니즘
   - h2 database 데이터를 파일 형태로 저장해 서버 다운 후 재기동시에도 기존 데이터 유지

## 시스템 구성

### 시스템 아키텍처

알림 프론트 서버는 클라이언트(모바일 앱, 웹 애플리케이션, 기타 시스템)와 알림 발송 서버 사이의 중개자 역할을 하며 다음을 제공합니다:

1. **즉시 알림 처리**:
   - 알림 요청을 즉시 검증하고 처리
   - 적절한 알림 채널로 라우팅

2. **예약 알림 처리**:
   - 향후 전송을 위한 알림 요청 저장
   - 백그라운드 배치 프로세스가 지정된 시간에 예약 및 발송

3. **장애 복구**:
   - 알림 전송 상태 추적
   - 실패한 알림 자동 재시도
   - 알림 이력 조회 기능 제공

### 데이터 흐름

1. 클라이언트가 알림 요청 제출
2. 프론트 서버가 검증하고 처리 큐에 추가
3. 비동기 워커가 알림 처리:
   - 즉시: 바로 처리
   - 예약: 향후 처리를 위해 저장
4. 발송 서버를 통해 적절한 채널로 알림 전송
5. 결과 저장 및 추적
6. API를 통해 이력 조회 가능

## API 명세

### 1. 알림 발송등록 API

**엔드포인트**: `POST /api/alarms`

**요청 본문**:
```json
{
  "clientType": "MOBILE|WEB|SYSTEM",
  "targetId": "customer123",
  "channel": "SMS|KAKAOTALK|EMAIL",
  "title": "알림 제목",
  "contents": "알림 내용",
  "scheduleTime": "yyyyMMddhhmm"
}
```

**응답**:
```json
{
  "success": true,
  "alarmId": 12345,
  "errorMessage": null
}
```

### 2. 알림내역조회 API

**엔드포인트**: `GET /api/alarms?customerId={id}&page={page}&size={size}`

**파라미터**:
- `customerId`: 조회할 고객 ID (필수)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

**응답**:
```json
{
  "content": [
    {
      "id": 12345,
      "channel": "SMS",
      "title": "알림 제목",
      "contents": "알림 내용",
      "status": "COMPLETED",
      "createdAt": "2024-03-15T14:30:00",
      "completedAt": "2024-03-15T14:30:05"
    }
  ],
  "totalElements": 42,
  "totalPages": 3,
  "size": 20,
  "number": 0
}
```

## 빌드 결과물 다운로드 링크

[실행 가능한 JAR 다운로드](https://github.com/Jae-KimSeo/20250323_2495-000111/raw/main/AlarmFront-0.0.1-SNAPSHOT.jar)

## 실행 방법

```
java -jar AlarmFront-0.0.1-SNAPSHOT.jar
```

이렇게 하면 기본적으로 포트 8080에서 애플리케이션이 시작됩니다.
