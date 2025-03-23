package org.service.alarmfront.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.exception.NotificationSendException;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ResultCode;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.batch.core.configuration.annotation.StepScope;

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
    
    @Value("${notification.retry.max-count:3}")
    private int maxRetryCount;

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
    @StepScope
    public ItemReader<NotificationRequest> failedNotificationReader() {
        List<NotificationRequest> failedNotifications = notificationRequestRepository
                .findRetryableNotifications(Status.FAILED, maxRetryCount);

        log.info("실패한 알림 {} 건 재시도 준비", failedNotifications.size());
        return new ListItemReader<>(failedNotifications);
    }

    @Bean
    public ItemProcessor<NotificationRequest, NotificationRequest> retryProcessor() {
        return request -> {
            int attemptCount = request.getAttemptCount();
            Channel channel = request.getChannel();
            NotificationSender sender = notificationSenders.get(channel);
            
            if (sender == null) {
                log.error("지원하지 않는 채널: {}", channel);
                request.updateStatus(Status.RETRY_EXHAUSTED);
                NotificationHistory history = NotificationHistory.create(
                    request, ResultCode.FAIL, "지원하지 않는 채널: " + channel, attemptCount + 1);
                request.addHistory(history);
                return request;
            }
            
            try {
                log.info("알림 재시도 발송: ID={}, 채널={}, 대상={}, 시도 횟수={}/{}",
                        request.getId(), channel, request.getTargetId(), attemptCount + 1, maxRetryCount);
                
                boolean success = sender.send(request);
                
                if (success) {
                    handleSuccess(request, attemptCount);
                } else {
                    handleFailure(request, "알림 재시도 실패: 결과 코드 FAIL");
                }
            } catch (Exception e) {
                handleException(request, e);
            }
            
            return request;
        };
    }

    @Bean
    public ItemWriter<NotificationRequest> notificationWriter() {
        return requests -> {
            try {
                notificationRequestRepository.saveAll(requests);
                log.debug("알림 상태 업데이트 완료: {} 건", requests.size());
            } catch (Exception e) {
                log.error("알림 상태 업데이트 중 오류 발생: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    private void handleSuccess(NotificationRequest request, int attemptCount) {
        log.info("알림 재시도 성공: ID={}", request.getId());
        request.updateStatus(Status.COMPLETED);
        NotificationHistory history = NotificationHistory.createSuccessHistory(
            request, attemptCount + 1);
        request.addHistory(history);
    }

    private void handleFailure(NotificationRequest request, String errorMessage) {
        int currentAttemptCount = request.getAttemptCount() + 1;
        log.error("알림 재시도 실패: ID={}, 오류={}, 시도 횟수={}/{}", 
                request.getId(), errorMessage, currentAttemptCount, maxRetryCount);

        if (currentAttemptCount >= maxRetryCount) {
            request.updateStatus(Status.RETRY_EXHAUSTED);
            log.info("최대 재시도 횟수 초과: ID={}, 상태를 RETRY_EXHAUSTED로 변경", request.getId());
        } else {
            request.updateStatus(Status.FAILED);
        }
        
        NotificationHistory history = NotificationHistory.createFailHistory(
                request, errorMessage, currentAttemptCount);
        request.addHistory(history);
    }

    private void handleException(NotificationRequest request, Exception e) {
        String errorMessage;
        if (e instanceof ResourceAccessException) {
            errorMessage = "알림 재시도 타임아웃: " + e.getMessage();
        } else if (e instanceof NotificationSendException) {
            errorMessage = "알림 재시도 예외 발생: " + e.getMessage();
        } else {
            errorMessage = "예기치 않은 오류: " + e.getMessage();
        }
        handleFailure(request, errorMessage);
    }
}