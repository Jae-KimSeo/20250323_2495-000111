package org.service.alarmfront.application.port.in;

import org.service.alarmfront.adapter.in.web.NotificationHistoryResponseDTO;
import org.springframework.data.domain.Page;

public interface InquireNotificationHistoryUseCase {
    Page<NotificationHistoryResponseDTO> getNotificationHistory(String customerId, int page, int size);
}
