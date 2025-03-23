package org.service.alarmfront.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final JobLauncher jobLauncher;
    private final Job processScheduledNotificationsJob;
    private final Job retryFailedNotificationsJob;

    @Scheduled(cron = "*/10 * * * * ?")
    public void runScheduledNotifications() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            log.info("예약 알림 처리 배치 작업 시작");
            jobLauncher.run(processScheduledNotificationsJob, jobParameters);
            log.info("예약 알림 처리 배치 작업 완료");
        } catch (JobExecutionAlreadyRunningException | JobRestartException 
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("예약 알림 처리 배치 작업 실행 중 오류 발생: {}", e.getMessage());
        }
    }
    
    @Scheduled(cron = "0 */5 * * * ?")
    public void retryFailedNotifications() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            
            log.info("실패한 알림 재시도 배치 작업 시작");
            jobLauncher.run(retryFailedNotificationsJob, jobParameters);
            log.info("실패한 알림 재시도 배치 작업 완료");
        } catch (JobExecutionAlreadyRunningException | JobRestartException 
                | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("실패한 알림 재시도 배치 작업 실행 중 오류 발생: {}", e.getMessage());
        }
    }
}
