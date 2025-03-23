package org.service.alarmfront.application.service;

import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.domain.value.Channel;

public abstract class AbstractAlarmRegistrationStrategy implements AlarmRegistrationStrategy {

    protected static final String PREFERRED_CHANNEL_KEY = "preferredChannel";

    protected Channel getChannelByName(String channelName) {
        try {
            if (channelName != null && !channelName.isEmpty()) {
                return Channel.valueOf(channelName.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            System.out.println("지원하지 않는 채널 이름: " + channelName);
        }
        return null;
    }

    protected Channel getPreferredChannelFromAdditionalData(AlarmCommand command) {
        if (command.getAdditionalData() != null && command.getAdditionalData().containsKey(PREFERRED_CHANNEL_KEY)) {
            String preferredChannel = String.valueOf(command.getAdditionalData().get(PREFERRED_CHANNEL_KEY));
            return getChannelByName(preferredChannel);
        }
        return null;
    }

    protected abstract Channel getDefaultChannel();
    
    @Override
    public Channel determineChannel(AlarmCommand command) {
        Channel preferredChannel = getPreferredChannelFromAdditionalData(command);
        if (preferredChannel != null) {
            return preferredChannel;
        }

        return getDefaultChannel();
    }
}