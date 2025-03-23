package org.service.alarmfront.adapter.in.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ResultCode;
import org.service.alarmfront.domain.value.Status;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationHistoryResponseDTO {
    private Long id;
    private String targetId;
    private Channel channel;
    private String contents;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledTime;
    private Status status;
    private Integer attemptCount;
    private ResultCode latestResultCode;

    public NotificationHistoryResponse toResponse() {
        return NotificationHistoryResponse.builder()
                .id(id)
                .targetId(targetId)
                .channel(channel.toString())
                .contents(contents)
                .createdAt(createdAt)
                .scheduledTime(scheduledTime)
                .status(status.toString())
                .attemptCount(attemptCount)
                .latestResultCode(latestResultCode != null ? latestResultCode.toString() : null)
                .build();
    }
}