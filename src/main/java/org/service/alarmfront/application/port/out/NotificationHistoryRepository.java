package org.service.alarmfront.application.port.out;

import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.ResultCode;

import java.util.List;
import java.util.Optional;

public interface NotificationHistoryRepository {
    NotificationHistory save(NotificationHistory notificationHistory);
    Optional<NotificationHistory> findById(Long id);
    List<NotificationHistory> findByRequest(NotificationRequest request);
    List<NotificationHistory> findByRequestId(Long requestId);
    List<NotificationHistory> findByResultCode(ResultCode resultCode);
    Integer countByRequestId(Long requestId);
} 