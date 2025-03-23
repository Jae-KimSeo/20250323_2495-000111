package org.service.alarmfront.application.port.out;

import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRequestRepository {

    NotificationRequest save(NotificationRequest request);
    Optional<NotificationRequest> findById(Long id);
    List<NotificationRequest> findByStatus(Status status);
}