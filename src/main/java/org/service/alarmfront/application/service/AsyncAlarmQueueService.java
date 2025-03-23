package org.service.alarmfront.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncAlarmQueueService {

    private final AlarmRegistrationService alarmRegistrationService;
    private final BlockingQueue<Long> alarmQueue = new LinkedBlockingQueue<>();

    public Long createAlarmRecord(AlarmCommand command) {
        Long alarmId = alarmRegistrationService.createAlarmRecord(command);
        alarmQueue.offer(alarmId);
        return alarmId;
    }

    @PostConstruct
    public void startQueueConsumer() {
        Thread worker = new Thread(this::consumeQueue, "AsyncAlarmWorker");
        worker.setDaemon(true);
        worker.start();
    }

    private void consumeQueue() {
        while(true) {
            try {
                Long alarmId = alarmQueue.take();
                alarmRegistrationService.processAlarmRecord(alarmId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("AsyncAlarmWorker 스레드가 인터럽트 되었습니다.", e);
                break;
            } catch (Exception ex) {
                log.error("알림 처리 중 예외 발생: {}", ex.getMessage(), ex);
            }
        }
    }
}