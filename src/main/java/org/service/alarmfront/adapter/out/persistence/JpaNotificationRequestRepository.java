package org.service.alarmfront.adapter.out.persistence;

import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaNotificationRequestRepository extends JpaRepository<NotificationRequest, Long> {
    List<NotificationRequest> findByStatus(Status status);
    List<NotificationRequest> findByStatusAndScheduledTimeBefore(Status status, LocalDateTime dateTime);
    
    @Modifying
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Query("UPDATE NotificationRequest r SET r.status = :status WHERE r.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Status status);

    @Query("SELECT r FROM NotificationRequest r WHERE r.status = :status AND r.attemptCount < :maxRetryCount")
    List<NotificationRequest> findRetryableNotifications(@Param("status") Status status, @Param("maxRetryCount") int maxRetryCount);
}