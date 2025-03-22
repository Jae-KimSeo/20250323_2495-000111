package org.service.alarmfront.application.service;

import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.domain.value.Channel;
import org.springframework.stereotype.Component;

@Component
public class SystemAlarmRegistrationStrategy extends AbstractAlarmRegistrationStrategy {
    
    @Override
    public void validateRequest(AlarmCommand command) {
        if (command.getAlarmType() == null || command.getAlarmType().isEmpty()) {
            throw new IllegalArgumentException("시스템 알림 타입은 필수입니다.");
        }
        
        if (command.getContent() == null || command.getContent().isEmpty()) {
            throw new IllegalArgumentException("알림 내용은 필수입니다.");
        }
    }
    
    @Override
    public void processClientSpecificLogic(AlarmCommand command) {
        System.out.println("시스템 클라이언트 알림 처리 로직 실행: " + command.getAlarmType());
    }
    
    @Override
    protected Channel getDefaultChannel() {
        return getDefaultChannelByAlarmType(null);
    }
    
    @Override
    public Channel determineChannel(AlarmCommand command) {
        Channel preferredChannel = getPreferredChannelFromAdditionalData(command);
        if (preferredChannel != null) {
            return preferredChannel;
        }

        return getDefaultChannelByAlarmType(command.getAlarmType());
    }
    
    private Channel getDefaultChannelByAlarmType(String alarmType) {
        if (alarmType == null || alarmType.isEmpty()) {
            return Channel.EMAIL;
        }

        return switch (alarmType.toLowerCase()) {
            case "notification", "alert" -> Channel.KAKAOTALK;
            case "reminder" -> Channel.SMS;
            default -> Channel.EMAIL;
        };
    }
}