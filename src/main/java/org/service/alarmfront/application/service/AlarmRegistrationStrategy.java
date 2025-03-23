package org.service.alarmfront.application.service;

import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.domain.value.Channel;

public interface AlarmRegistrationStrategy {
    void validateRequest(AlarmCommand command);
    void processClientSpecificLogic(AlarmCommand command);
    Channel determineChannel(AlarmCommand command);
}