package org.service.alarmfront.application.port.in;

public interface RegisterAlarmUseCase {
    Long registerAlarm(AlarmCommand command);
}