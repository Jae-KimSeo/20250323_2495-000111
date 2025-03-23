package org.service.alarmfront.adapter.out.api;

import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Primary
@Profile("test")
public class MockKakaoNotificationSender implements NotificationSender {
    
    @Override
    public boolean send(NotificationRequest request) {
        return true;
    }
} 