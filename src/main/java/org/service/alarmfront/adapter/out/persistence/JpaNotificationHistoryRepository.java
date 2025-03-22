package org.service.alarmfront.adapter.out.persistence;

import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.ResultCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaNotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    List<NotificationHistory> findByRequest(NotificationRequest request);
    List<NotificationHistory> findByRequestId(Long requestId);
    List<NotificationHistory> findByResultCode(ResultCode resultCode);

    @Query("SELECT COUNT(h) FROM NotificationHistory h WHERE h.request.id = :requestId")
    Integer countByRequestId(@Param("requestId") Long requestId);
}