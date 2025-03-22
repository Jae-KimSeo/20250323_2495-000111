package org.service.alarmfront.application.service;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.service.alarmfront.domain.value.Channel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class AlarmRegistrationService implements RegisterAlarmUseCase {

    private final AlarmRegistrationStrategyFactory strategyFactory;
    private final AtomicLong alarmIdGenerator = new AtomicLong(1);
    
    @Override
    public Long registerAlarm(AlarmCommand command) {
        AlarmRegistrationStrategy strategy = strategyFactory.getStrategy(command.getClientType());
        strategy.validateRequest(command);

        Long alarmId = alarmIdGenerator.getAndIncrement();
        Channel channel = strategy.determineChannel(command);

        if (command.getScheduledTime() == null || command.getScheduledTime().isBefore(LocalDateTime.now())) {
            sendAlarmImmediately(alarmId, command, channel);
        } else {
            scheduleAlarm(alarmId, command, channel);
        }

        strategy.processClientSpecificLogic(command);
        
        return alarmId;
    }
    
    private void sendAlarmImmediately(Long alarmId, AlarmCommand command, Channel channel) {
        System.out.println("알림 즉시 발송: ID=" + alarmId + ", 타입=" + command.getAlarmType() + ", 채널=" + channel);
    }
    
    private void scheduleAlarm(Long alarmId, AlarmCommand command, Channel channel) {
        System.out.println("알림 예약 발송 등록: ID=" + alarmId + ", 시각=" + command.getScheduledTime() + ", 채널=" + channel);
    }
}