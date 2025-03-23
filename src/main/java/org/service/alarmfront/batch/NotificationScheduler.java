package org.service.alarmfront.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final JobLauncher jobLauncher;
    private final Job processScheduledNotificationsJob;
    private final Job retryFailedNotificationsJob;


    @Scheduled(fixedDelay = 10000)
    public void runScheduledNotifications() {
        executeJob(processScheduledNotificationsJob, "예약 알림 처리");
    }
    
    @Scheduled(fixedDelay = 20000)
    public void retryFailedNotifications() {
        executeJob(retryFailedNotificationsJob, "실패한 알림 재시도");
    }
    
    private void executeJob(Job job, String jobName) {
        try {
            Date now = new Date();
            
            JobParameters jobParameters = new JobParametersBuilder()
                    .addDate("time", now)
                    .toJobParameters();
            
            log.info("{} 배치 작업 시작 - 파라미터: {}", jobName, jobParameters);
            jobLauncher.run(job, jobParameters);
            log.info("{} 배치 작업 완료", jobName);
        } catch (Exception e) {
            log.error("{} 배치 작업 실행 중 오류 발생: {}", jobName, e.getMessage(), e);
        }
    }
}
