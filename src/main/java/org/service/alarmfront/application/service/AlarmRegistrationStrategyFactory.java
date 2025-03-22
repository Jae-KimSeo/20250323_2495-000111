package org.service.alarmfront.application.service;

import org.service.alarmfront.domain.value.ClientType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class AlarmRegistrationStrategyFactory {
    
    private final Map<ClientType, AlarmRegistrationStrategy> strategies;
    
    public AlarmRegistrationStrategyFactory(
            MobileAlarmRegistrationStrategy mobileStrategy,
            WebAlarmRegistrationStrategy webStrategy,
            SystemAlarmRegistrationStrategy systemStrategy) {
        
        strategies = new EnumMap<>(ClientType.class);
        strategies.put(ClientType.MOBILE, mobileStrategy);
        strategies.put(ClientType.WEB, webStrategy);
        strategies.put(ClientType.SYSTEM, systemStrategy);
    }
    
    public AlarmRegistrationStrategy getStrategy(ClientType clientType) {
        AlarmRegistrationStrategy strategy = strategies.get(clientType);
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 클라이언트 타입입니다: " + clientType);
        }
        return strategy;
    }
}