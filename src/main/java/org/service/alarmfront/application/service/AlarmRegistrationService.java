package org.service.alarmfront.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ResultCode;
import org.service.alarmfront.domain.value.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmRegistrationService implements RegisterAlarmUseCase {

    private final AlarmRegistrationStrategyFactory strategyFactory;
    private final NotificationRequestRepository notificationRequestRepository;
    
    @Override
    @Transactional
    public Long registerAlarm(AlarmCommand command) {
        AlarmRegistrationStrategy strategy = strategyFactory.getStrategy(command.getClientType());
        strategy.validateRequest(command);
        Channel channel = strategy.determineChannel(command);
        
        NotificationRequest request = createNotificationRequest(command, channel);
        
        if (command.getScheduledTime() == null || command.getScheduledTime().isBefore(LocalDateTime.now())) {
            request.updateStatus(Status.PROCESSING);
            NotificationRequest savedRequest = notificationRequestRepository.save(request);
            sendAlarmImmediately(savedRequest, channel);
        } else {
            request.updateStatus(Status.SCHEDULED);
            NotificationRequest savedRequest = notificationRequestRepository.save(request);
            log.info("알림 예약 발송 등록: ID={}, 시각={}, 채널={}", 
                    savedRequest.getId(), command.getScheduledTime(), channel);
        }
        
        strategy.processClientSpecificLogic(command);
        
        return request.getId();
    }
    
    private NotificationRequest createNotificationRequest(AlarmCommand command, Channel channel) {
        return NotificationRequest.create(
                command.getTargetId(),
                channel,
                command.getContent(),
                command.getScheduledTime()
        );
    }
    
    private void sendAlarmImmediately(NotificationRequest request, Channel channel) {
        try {
            log.info("알림 즉시 발송: ID={}, 채널={}, 대상={}",
                    request.getId(), channel, request.getTargetId());
            
            NotificationHistory history = NotificationHistory.createSuccessHistory(
                    request, request.getAttemptCount() + 1);
            request.addHistory(history);
            request.updateStatus(Status.COMPLETED);
            
            notificationRequestRepository.save(request);
            
            log.info("알림 발송 성공: ID={}", request.getId());
        } catch (Exception e) {
            log.error("알림 발송 실패: ID={}, 오류={}", request.getId(), e.getMessage());
            NotificationHistory history = NotificationHistory.createFailHistory(
                    request, e.getMessage(), request.getAttemptCount() + 1);
            request.addHistory(history);
            request.updateStatus(Status.FAILED);
            
            notificationRequestRepository.save(request);
        }
    }
}