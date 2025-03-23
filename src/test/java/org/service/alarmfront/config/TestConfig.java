package org.service.alarmfront.config;

import org.mockito.Mockito;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@TestConfiguration
public class TestConfig {
    
    public static CountDownLatch alarmProcessingLatch = new CountDownLatch(0);
    
    @Bean
    @Primary
    public Map<Channel, NotificationSender> testNotificationSenders() {
        Map<Channel, NotificationSender> senders = new HashMap<>();

        NotificationSender smsSender = Mockito.mock(NotificationSender.class);
        Mockito.when(smsSender.send(Mockito.any(NotificationRequest.class))).thenAnswer(invocation -> {
            alarmProcessingLatch.countDown();
            return true;
        });
        senders.put(Channel.SMS, smsSender);

        NotificationSender emailSender = Mockito.mock(NotificationSender.class);
        Mockito.when(emailSender.send(Mockito.any(NotificationRequest.class))).thenAnswer(invocation -> {
            alarmProcessingLatch.countDown();
            return true;
        });
        senders.put(Channel.EMAIL, emailSender);

        NotificationSender kakaoSender = Mockito.mock(NotificationSender.class);
        Mockito.when(kakaoSender.send(Mockito.any(NotificationRequest.class))).thenAnswer(invocation -> {
            alarmProcessingLatch.countDown();
            return true;
        });
        senders.put(Channel.KAKAOTALK, kakaoSender);
        
        return senders;
    }
    
    public static void resetLatch(int count) {
        alarmProcessingLatch = new CountDownLatch(count);
    }
} 