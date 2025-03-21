package org.service.alarmfront.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.out.NotificationHistoryRepository;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.ResultCode;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class NotificationHistoryPersistenceAdapter implements NotificationHistoryRepository {

    private final JpaNotificationHistoryRepository jpaNotificationHistoryRepository;

    @Override
    public NotificationHistory save(NotificationHistory notificationHistory) {
        return jpaNotificationHistoryRepository.save(notificationHistory);
    }

    @Override
    public Optional<NotificationHistory> findById(Long id) {
        return jpaNotificationHistoryRepository.findById(id);
    }

    @Override
    public List<NotificationHistory> findByRequest(NotificationRequest request) {
        return jpaNotificationHistoryRepository.findByRequest(request);
    }

    @Override
    public List<NotificationHistory> findByRequestId(Long requestId) {
        return jpaNotificationHistoryRepository.findByRequestId(requestId);
    }

    @Override
    public List<NotificationHistory> findByResultCode(ResultCode resultCode) {
        return jpaNotificationHistoryRepository.findByResultCode(resultCode);
    }

    @Override
    public Integer countByRequestId(Long requestId) {
        return jpaNotificationHistoryRepository.countByRequestId(requestId);
    }
} 