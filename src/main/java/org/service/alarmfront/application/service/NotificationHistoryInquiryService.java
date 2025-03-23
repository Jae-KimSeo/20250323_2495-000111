package org.service.alarmfront.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.in.InquireNotificationHistoryUseCase;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationHistoryInquiryService implements InquireNotificationHistoryUseCase {

    private final NotificationRequestRepository notificationRequestRepository;

    @Override
    public Page<NotificationRequest> getNotificationHistory(String customerId, int page, int size) {
        log.info("Retrieving notification history for customer: {}, page: {}, size: {}", customerId, page, size);
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        return notificationRequestRepository.findByTargetIdAndCreatedAfter(customerId, threeMonthsAgo, pageable);
    }
}
