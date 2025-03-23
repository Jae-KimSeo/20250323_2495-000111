package org.service.alarmfront.application.port.in;

import lombok.Builder;
import lombok.Getter;
import org.service.alarmfront.domain.value.ClientType;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class AlarmCommand {
    private final ClientType clientType;
    private final String alarmType;
    private final String targetId;
    private final String content;
    private final LocalDateTime scheduledTime;
    private final Map<String, Object> additionalData;
}