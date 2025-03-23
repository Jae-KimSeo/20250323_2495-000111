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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationRetryBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NotificationRequestRepository notificationRequestRepository;
    private final Map<Channel, NotificationSender> notificationSenders;
    
    @Bean
    public Job retryFailedNotificationsJob() {
        return new JobBuilder("retryFailedNotificationsJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(retryFailedNotificationsStep())
                .end()
                .build();
    }

    @Bean
    public Step retryFailedNotificationsStep() {
        return new StepBuilder("retryFailedNotificationsStep", jobRepository)
                .<NotificationRequest, NotificationRequest>chunk(10, transactionManager)
                .reader(failedNotificationReader())
                .processor(retryProcessor())
                .writer(notificationWriter())
                .build();
    }

    @Bean
    public ItemReader<NotificationRequest> failedNotificationReader() {
        return new ItemReader<NotificationRequest>() {
            private List<NotificationRequest> failedNotifications;
            private int currentIndex = 0;
            
            @Override
            public NotificationRequest read() {
                if (failedNotifications == null) {
                    failedNotifications = notificationRequestRepository.findByStatus(Status.FAILED);
                    log.info("실패한 알림 {} 건 재시도 준비", failedNotifications.size());
                }
                
                if (currentIndex < failedNotifications.size()) {
                    return failedNotifications.get(currentIndex++);
                }

                return null;
            }
        };
    }

    @Bean
    public ItemProcessor<NotificationRequest, NotificationRequest> retryProcessor() {
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
                log.info("알림 재시도 발송: ID={}, 채널={}, 대상={}", 
                        request.getId(), channel, request.getTargetId());
                
                boolean success = sender.send(request);
                
                if (success) {
                    log.info("알림 재시도 성공: ID={}", request.getId());
                    request.updateStatus(Status.COMPLETED);
                    NotificationHistory history = NotificationHistory.createSuccessHistory(
                            request, request.getAttemptCount() + 1);
                    request.addHistory(history);
                } else {
                    String errorMessage = "알림 재시도 실패: 결과 코드 FAIL";
                    log.error("알림 재시도 실패: ID={}, 오류={}", request.getId(), errorMessage);
                    createFailHistory(request, errorMessage);
                }
            } catch (ResourceAccessException e) {
                String errorMessage = "알림 재시도 타임아웃: " + e.getMessage();
                log.error("알림 재시도 타임아웃: ID={}, 오류={}", request.getId(), e.getMessage());
                createFailHistory(request, errorMessage);
            } catch (NotificationSendException e) {
                log.error("알림 재시도 예외 발생: ID={}, 오류={}", request.getId(), e.getMessage());
                createFailHistory(request, e.getMessage());
            } catch (Exception e) {
                log.error("알림 재시도 중 예기치 않은 오류: ID={}, 오류={}", request.getId(), e.getMessage());
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
