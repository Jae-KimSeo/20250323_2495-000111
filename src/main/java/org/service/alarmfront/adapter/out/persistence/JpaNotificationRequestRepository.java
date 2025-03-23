package org.service.alarmfront.adapter.out.persistence;

import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaNotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {
    List<NotificationRequest> findByStatus(Status status);
    List<NotificationRequest> findByStatusAndScheduledTimeBefore(Status status, LocalDateTime dateTime);
} 