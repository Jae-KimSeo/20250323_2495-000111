package org.service.alarmfront.adapter.in.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.domain.value.ClientType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRegistrationRequest {
    private String clientType;
    private String alarmType;
    private String targetId;
    private String content;
    private String scheduledTime;
    private Map<String, Object> additionalData;
    
    public AlarmCommand toCommand() {
        ClientType clientTypeEnum = ClientType.valueOf(clientType.toUpperCase());
        
        LocalDateTime scheduledDateTime = null;
        if (scheduledTime != null && !scheduledTime.isEmpty()) {
            scheduledDateTime = LocalDateTime.parse(
                scheduledTime, 
                DateTimeFormatter.ofPattern("yyyyMMddHHmm")
            );
        }
        
        return AlarmCommand.builder()
                .clientType(clientTypeEnum)
                .alarmType(alarmType)
                .targetId(targetId)
                .content(content)
                .scheduledTime(scheduledDateTime)
                .additionalData(additionalData)
                .build();
    }
}