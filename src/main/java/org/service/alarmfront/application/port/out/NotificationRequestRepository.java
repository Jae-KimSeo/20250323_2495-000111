package org.service.alarmfront.application.port.out;

import org.service.alarmfront.adapter.in.web.NotificationHistoryResponseDTO;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface NotificationRequestRepository {

    NotificationRequest save(NotificationRequest request);
    <S extends NotificationRequest> List<S> saveAll(Iterable<S> requests);
    Optional<NotificationRequest> findById(Long id);
    List<NotificationRequest> findByStatus(Status status);
    List<NotificationRequest> findScheduledNotifications(LocalDateTime beforeTime);
    void updateStatus(Long id, Status status);
    List<NotificationRequest> findRetryableNotifications(Status status, int maxRetryCount);
    Page<NotificationRequest> findRecentNotificationsByTargetId(String targetId, LocalDateTime startDate, Pageable pageable);
    Page<NotificationHistoryResponseDTO> findNotificationHistoryByTargetId(String targetId, LocalDateTime startDate, Pageable pageable);
    void deleteNotificationsOlderThan(LocalDateTime olderThan);
}