package org.service.alarmfront.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.service.alarmfront.domain.value.ResultCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_histories", indexes = {
    @Index(name = "idx_notification_history_request_id", columnList = "request_id")
})
@Getter
@NoArgsConstructor
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private NotificationRequest request;

    @Column(name = "attempt_time", nullable = false)
    private LocalDateTime attemptTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "result_code", nullable = false)
    private ResultCode resultCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static NotificationHistory create(NotificationRequest request, ResultCode resultCode, String errorMessage, Integer attemptCount) {
        NotificationHistory history = new NotificationHistory();
        history.request = request;
        history.attemptTime = LocalDateTime.now();
        history.resultCode = resultCode;
        history.errorMessage = errorMessage;
        history.attemptCount = attemptCount;
        return history;
    }

    public static NotificationHistory createSuccessHistory(NotificationRequest request, Integer attemptCount) {
        return create(request, ResultCode.SUCCESS, null, attemptCount);
    }

    public static NotificationHistory createFailHistory(NotificationRequest request, String errorMessage, Integer attemptCount) {
        return create(request, ResultCode.FAIL, errorMessage, attemptCount);
    }
} 