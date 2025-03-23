package org.service.alarmfront.adapter.out.persistence;

import org.service.alarmfront.adapter.in.web.NotificationHistoryResponseDTO;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Status;
import org.springframework.data.domain.Page;
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
    
    @Query("SELECT r FROM NotificationRequest r WHERE r.targetId = :targetId AND r.createdAt >= :startDate ORDER BY r.createdAt DESC")
    Page<NotificationRequest> findRecentNotificationsByTargetId(@Param("targetId") String targetId, @Param("startDate") LocalDateTime startDate, Pageable pageable);
    
    @Query("SELECT new org.service.alarmfront.adapter.in.web.NotificationHistoryResponseDTO(" +
           "r.id, r.targetId, r.channel, r.contents, r.createdAt, r.scheduledTime, r.status, r.attemptCount, " +
           "(SELECT h.resultCode FROM NotificationHistory h WHERE h.request.id = r.id ORDER BY h.createdAt DESC LIMIT 1)) " +
           "FROM NotificationRequest r " +
           "WHERE r.targetId = :targetId AND r.createdAt >= :startDate " +
           "ORDER BY r.createdAt DESC")
    Page<NotificationHistoryResponseDTO> findNotificationHistoryByTargetId(
        @Param("targetId") String targetId, 
        @Param("startDate") LocalDateTime startDate, 
        Pageable pageable);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationRequest r WHERE r.createdAt < :olderThan")
    void deleteNotificationsOlderThan(@Param("olderThan") LocalDateTime olderThan);
}