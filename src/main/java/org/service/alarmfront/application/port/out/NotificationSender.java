package org.service.alarmfront.application.port.out;

import org.service.alarmfront.domain.entity.NotificationRequest;

public interface NotificationSender {
    boolean send(NotificationRequest request);
    String checkStatus(Long requestId);
}