package org.service.alarmfront.application.service;

import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.domain.value.Channel;
import org.springframework.stereotype.Component;

@Component
public class WebAlarmRegistrationStrategy extends AbstractAlarmRegistrationStrategy {
    
    @Override
    public void validateRequest(AlarmCommand command) {
        if (command.getContent() == null || command.getContent().isEmpty()) {
            throw new IllegalArgumentException("웹 알림 내용은 필수입니다.");
        }
        
        if (command.getTargetId() == null || command.getTargetId().isEmpty()) {
            throw new IllegalArgumentException("웹 알림 대상 ID는 필수입니다.");
        }
    }
    
    @Override
    public void processClientSpecificLogic(AlarmCommand command) {
        System.out.println("웹 클라이언트 알림 처리 로직 실행: " + command.getContent());
    }
    
    @Override
    protected Channel getDefaultChannel() {
        return Channel.EMAIL;
    }
}