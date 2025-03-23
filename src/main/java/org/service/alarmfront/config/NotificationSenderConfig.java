package org.service.alarmfront.config;

import org.service.alarmfront.adapter.out.api.EmailNotificationSender;
import org.service.alarmfront.adapter.out.api.KakaotalkNotificationSender;
import org.service.alarmfront.adapter.out.api.SmsNotificationSender;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.value.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.EnumMap;
import java.util.Map;

@Configuration
public class NotificationSenderConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        
        return new RestTemplate(factory);
    }

    @Bean
    public Map<Channel, NotificationSender> notificationSenders(
            SmsNotificationSender smsNotificationSender,
            KakaotalkNotificationSender kakaotalkNotificationSender,
            EmailNotificationSender emailNotificationSender) {
        
        Map<Channel, NotificationSender> senders = new EnumMap<>(Channel.class);
        senders.put(Channel.SMS, smsNotificationSender);
        senders.put(Channel.KAKAOTALK, kakaotalkNotificationSender);
        senders.put(Channel.EMAIL, emailNotificationSender);
        
        return senders;
    }
}