package org.service.alarmfront.adapter.in.web;

import lombok.Builder;
import lombok.Getter;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.ResultCode;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
public class NotificationHistoryResponse {
    private Long id;
    private String targetId;
    private String channel;
    private String contents;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledTime;
    private String status;
    private Integer attemptCount;
    private String latestResultCode;
    
    public static NotificationHistoryResponse fromEntity(NotificationRequest notification) {
        String resultCode = null;
        if (!notification.getHistories().isEmpty()) {
            resultCode = notification.getHistories().stream()
                    .max((h1, h2) -> h1.getCreatedAt().compareTo(h2.getCreatedAt()))
                    .map(h -> h.getResultCode().toString())
                    .orElse(null);
        }
        
        return NotificationHistoryResponse.builder()
                .id(notification.getId())
                .targetId(notification.getTargetId())
                .channel(notification.getChannel().toString())
                .contents(notification.getContents())
                .createdAt(notification.getCreatedAt())
                .scheduledTime(notification.getScheduledTime())
                .status(notification.getStatus().toString())
                .attemptCount(notification.getAttemptCount())
                .latestResultCode(resultCode)
                .build();
    }
}
