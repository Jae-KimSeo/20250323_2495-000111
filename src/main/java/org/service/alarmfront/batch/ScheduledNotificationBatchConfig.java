package org.service.alarmfront.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.exception.NotificationSendException;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.Status;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.batch.core.configuration.annotation.StepScope;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledNotificationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationRequestRepository notificationRequestRepository;
    private final Map<Channel, NotificationSender> notificationSenders;
    
    @Bean
    public Job processScheduledNotificationsJob() {
        return new JobBuilder("processScheduledNotificationsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(processScheduledNotificationsStep())
                .end()
                .build();
    }

    @Bean
    public Step processScheduledNotificationsStep() {
        return new StepBuilder("processScheduledNotificationsStep", jobRepository)
                .<NotificationRequest, NotificationRequest>chunk(10, transactionManager)
                .reader(scheduledNotificationReader())
                .processor(notificationProcessor())
                .writer(notificationWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<NotificationRequest> scheduledNotificationReader() {
        LocalDateTime now = LocalDateTime.now();
        List<NotificationRequest> scheduledNotifications =
                notificationRequestRepository.findScheduledNotifications(now);
        log.info("예약된 알림 {} 건 조회", scheduledNotifications.size());
        return new ListItemReader<>(scheduledNotifications);
    }

    @Bean
    public ItemProcessor<NotificationRequest, NotificationRequest> notificationProcessor() {
        return request -> {
            Channel channel = request.getChannel();
            NotificationSender sender = notificationSenders.get(channel);
            
            if (sender == null) {
                String errorMessage = "지원하지 않는 채널: " + channel;
                log.error(errorMessage);
                createFailHistory(request, errorMessage);
                return request;
            }
            
            try {
                log.info("예약 알림 발송 시도: ID={}, 채널={}, 대상={}", 
                        request.getId(), channel, request.getTargetId());
                
                boolean success = sender.send(request);
                
                if (success) {
                    log.info("예약 알림 발송 성공: ID={}", request.getId());
                    request.updateStatus(Status.COMPLETED);
                    NotificationHistory history = NotificationHistory.createSuccessHistory(
                            request, request.getAttemptCount() + 1);
                    request.addHistory(history);
                } else {
                    String errorMessage = "예약 알림 발송 실패: 결과 코드 FAIL";
                    log.error("예약 알림 발송 실패: ID={}, 오류={}", request.getId(), errorMessage);
                    createFailHistory(request, errorMessage);
                }
            } catch (ResourceAccessException e) {
                String errorMessage = "예약 알림 발송 타임아웃: " + e.getMessage();
                log.error("예약 알림 발송 타임아웃: ID={}, 오류={}", request.getId(), e.getMessage());
                createFailHistory(request, errorMessage);
            } catch (NotificationSendException e) {
                log.error("예약 알림 발송 예외 발생: ID={}, 오류={}", request.getId(), e.getMessage());
                createFailHistory(request, e.getMessage());
            } catch (Exception e) {
                log.error("예약 알림 발송 중 예기치 않은 오류: ID={}, 오류={}", request.getId(), e.getMessage());
                createFailHistory(request, "예기치 않은 오류: " + e.getMessage());
            }
            
            return request;
        };
    }

    @Bean
    public ItemWriter<NotificationRequest> notificationWriter() {
        return notificationRequestRepository::saveAll;
    }
    
    private void createFailHistory(NotificationRequest request, String errorMessage) {
        request.updateStatus(Status.FAILED);
        NotificationHistory history = NotificationHistory.createFailHistory(
                request, errorMessage, request.getAttemptCount() + 1);
        request.addHistory(history);
    }
}