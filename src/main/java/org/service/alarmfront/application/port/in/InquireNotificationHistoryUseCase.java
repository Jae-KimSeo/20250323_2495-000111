package org.service.alarmfront.application.port.in;

import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.data.domain.Page;

public interface InquireNotificationHistoryUseCase {
    Page<NotificationRequest> getNotificationHistory(String customerId, int page, int size);
}
