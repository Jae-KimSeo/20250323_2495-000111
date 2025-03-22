package org.service.alarmfront.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.exception.NotificationSendException;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.Status;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmRegistrationService implements RegisterAlarmUseCase {

    private final AlarmRegistrationStrategyFactory strategyFactory;
    private final NotificationRequestRepository notificationRequestRepository;
    private final Map<Channel, NotificationSender> notificationSenders;
    
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
            notificationRequestRepository.save(request);
            log.info("알림 예약 발송 등록: ID={}, 시각={}, 채널={}", 
                    request.getId(), command.getScheduledTime(), channel);
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
        NotificationSender sender = notificationSenders.get(channel);
        if (sender == null) {
            String errorMessage = "지원하지 않는 채널: " + channel;
            log.error(errorMessage);
            createFailHistory(request, errorMessage);
            notificationRequestRepository.save(request);
            return;
        }
        
        try {
            log.info("알림 발송 시도: ID={}, 채널={}, 대상={}", 
                    request.getId(), channel, request.getTargetId());
            
            boolean success = sender.send(request);
            
            if (success) {
                log.info("알림 발송 성공: ID={}", request.getId());
                request.updateStatus(Status.COMPLETED);
                NotificationHistory history = NotificationHistory.createSuccessHistory(
                        request, request.getAttemptCount() + 1);
                request.addHistory(history);
            } else {
                String errorMessage = "알림 발송 실패: 결과 코드 FAIL";
                log.error("알림 발송 실패: ID={}, 오류={}", request.getId(), errorMessage);
                createFailHistory(request, errorMessage);
            }

            notificationRequestRepository.save(request);
        } catch (ResourceAccessException e) {
            String errorMessage = "알림 발송 타임아웃: " + e.getMessage();
            log.error("알림 발송 타임아웃: ID={}, 오류={}", request.getId(), e.getMessage());
            createFailHistory(request, errorMessage);
            notificationRequestRepository.save(request);
        } catch (NotificationSendException e) {
            log.error("알림 발송 예외 발생: ID={}, 오류={}", request.getId(), e.getMessage());
            createFailHistory(request, e.getMessage());
            notificationRequestRepository.save(request);
        } catch (Exception e) {
            log.error("알림 발송 중 예기치 않은 오류: ID={}, 오류={}", request.getId(), e.getMessage());
            createFailHistory(request, "예기치 않은 오류: " + e.getMessage());
            notificationRequestRepository.save(request);
        }
    }
    
    private void createFailHistory(NotificationRequest request, String errorMessage) {
        request.updateStatus(Status.FAILED);
        NotificationHistory history = NotificationHistory.createFailHistory(
                request, errorMessage, request.getAttemptCount() + 1);
        request.addHistory(history);
    }
}