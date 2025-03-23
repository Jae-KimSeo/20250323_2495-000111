package org.service.alarmfront.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationRequestPersistenceAdapter implements NotificationRequestRepository {

    private final JpaNotificationRequestRepository jpaNotificationRequestRepository;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public NotificationRequest save(NotificationRequest notificationRequest) {
        return jpaNotificationRequestRepository.save(notificationRequest);
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void updateStatus(Long id, Status status) {
        jpaNotificationRequestRepository.updateStatus(id, status);
    }
    
    @Override
    public List<NotificationRequest> findRetryableNotifications(Status status, int maxRetryCount) {
        return jpaNotificationRequestRepository.findRetryableNotifications(status, maxRetryCount);
    }
    
    @Override
    public Page<NotificationRequest> findRecentNotificationsByTargetId(String targetId, LocalDateTime startDate, Pageable pageable) {
        return jpaNotificationRequestRepository.findRecentNotificationsByTargetId(targetId, startDate, pageable);
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteNotificationsOlderThan(LocalDateTime olderThan) {
        jpaNotificationRequestRepository.deleteNotificationsOlderThan(olderThan);
    }
}