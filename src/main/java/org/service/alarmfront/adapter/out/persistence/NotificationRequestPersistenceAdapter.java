package org.service.alarmfront.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRequestPersistenceAdapter implements NotificationRequestRepository {

    private final JpaNotificationRequestRepository jpaNotificationRequestRepository;

    @Override
    public NotificationRequest save(NotificationRequest notificationRequest) {
        return jpaNotificationRequestRepository.save(notificationRequest);
    }
    
    @Override
    public <S extends NotificationRequest> List<S> saveAll(Iterable<S> requests) {
        return jpaNotificationRequestRepository.saveAll(requests);
    }

    @Override
    public Optional<NotificationRequest> findById(Long id) {
        return jpaNotificationRequestRepository.findById(id);
    }

    @Override
    public List<NotificationRequest> findByStatus(Status status) {
        return jpaNotificationRequestRepository.findByStatus(status);
    }
    
    @Override
    public List<NotificationRequest> findScheduledNotifications(LocalDateTime beforeTime) {
        return jpaNotificationRequestRepository.findByStatusAndScheduledTimeBefore(Status.SCHEDULED, beforeTime);
    }
} 