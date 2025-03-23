package org.service.alarmfront.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.config.TestConfig;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ClientType;
import org.service.alarmfront.domain.value.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
public class AsyncAlarmQueueServiceIntegrationTest {

    @Autowired
    private AsyncAlarmQueueService asyncAlarmQueueService;
    
    @Autowired
    private NotificationRequestRepository notificationRequestRepository;

    @BeforeEach
    void setUp() {
        TestConfig.resetLatch(1);
    }

    @Test
    void createAlarmRecord_ImmediateSending_Success() throws Exception {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("preferredChannel", "SMS");
        
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.MOBILE)
                .alarmType("alert")
                .targetId("user123")
                .content("비동기 테스트 알림")
                .additionalData(additionalData)
                .build();

        Long alarmId = asyncAlarmQueueService.createAlarmRecord(command);

        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        boolean processed = TestConfig.alarmProcessingLatch.await(5, TimeUnit.SECONDS);
        assertTrue(processed, "알림 처리가 시간 내에 완료되지 않았습니다.");

        Thread.sleep(1000);

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest savedRequest = optionalRequest.get();
        assertEquals(Status.COMPLETED, savedRequest.getStatus(), "알림 상태가 올바르게 업데이트되지 않았습니다.");
        assertEquals(Channel.SMS, savedRequest.getChannel(), "알림 채널이 올바르게 설정되지 않았습니다.");
        assertEquals("user123", savedRequest.getTargetId(), "알림 대상 ID가 올바르게 설정되지 않았습니다.");
    }

    @Test
    void createAlarmRecord_ScheduledSending_Success() throws Exception {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.WEB)
                .alarmType("reminder")
                .targetId("user123")
                .content("예약 비동기 테스트 알림")
                .scheduledTime(scheduledTime)
                .build();

        Long alarmId = asyncAlarmQueueService.createAlarmRecord(command);

        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        Thread.sleep(1000);

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest savedRequest = optionalRequest.get();
        assertEquals(Status.SCHEDULED, savedRequest.getStatus(), "예약 알림 상태가 올바르게 설정되지 않았습니다.");
        assertEquals(Channel.EMAIL, savedRequest.getChannel(), "알림 채널이 올바르게 설정되지 않았습니다.");
        assertEquals(scheduledTime, savedRequest.getScheduledTime(), "예약 시간이 올바르게 설정되지 않았습니다.");
    }

    @Test
    void createAlarmRecord_SystemType_Success() throws Exception {
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.SYSTEM)
                .alarmType("notification")
                .targetId("user123")
                .content("시스템 알림 테스트")
                .build();

        Long alarmId = asyncAlarmQueueService.createAlarmRecord(command);

        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        boolean processed = TestConfig.alarmProcessingLatch.await(5, TimeUnit.SECONDS);
        assertTrue(processed, "알림 처리가 시간 내에 완료되지 않았습니다.");

        Thread.sleep(1000);

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest savedRequest = optionalRequest.get();
        assertEquals(Status.COMPLETED, savedRequest.getStatus(), "알림 상태가 올바르게 업데이트되지 않았습니다.");
        assertEquals(Channel.KAKAOTALK, savedRequest.getChannel(), "시스템 알림 타입에 따른 채널이 올바르게 설정되지 않았습니다.");
    }

    @Test
    void createAlarmRecord_MultipleAlarms_Success() throws Exception {
        TestConfig.resetLatch(3);

        AlarmCommand command1 = AlarmCommand.builder()
                .clientType(ClientType.MOBILE)
                .targetId("multiUser1")
                .content("다중 알림 테스트 1")
                .build();
                
        AlarmCommand command2 = AlarmCommand.builder()
                .clientType(ClientType.WEB)
                .targetId("multiUser2")
                .content("다중 알림 테스트 2")
                .build();
                
        AlarmCommand command3 = AlarmCommand.builder()
                .clientType(ClientType.SYSTEM)
                .alarmType("alert")
                .targetId("multiUser3")
                .content("다중 알림 테스트 3")
                .build();

        Long alarmId1 = asyncAlarmQueueService.createAlarmRecord(command1);
        Long alarmId2 = asyncAlarmQueueService.createAlarmRecord(command2);
        Long alarmId3 = asyncAlarmQueueService.createAlarmRecord(command3);

        assertNotNull(alarmId1, "첫 번째 알림 ID가 생성되지 않았습니다.");
        assertNotNull(alarmId2, "두 번째 알림 ID가 생성되지 않았습니다.");
        assertNotNull(alarmId3, "세 번째 알림 ID가 생성되지 않았습니다.");

        boolean processed = TestConfig.alarmProcessingLatch.await(10, TimeUnit.SECONDS);
        assertTrue(processed, "알림 처리가 시간 내에 완료되지 않았습니다.");

        Page<NotificationRequest> requests = notificationRequestRepository.findRecentNotificationsByTargetId(
                "multiUser1", LocalDateTime.now().minusDays(1), PageRequest.of(0, 10));
        assertFalse(requests.isEmpty(), "첫 번째 알림이 저장되지 않았습니다.");
        
        requests = notificationRequestRepository.findRecentNotificationsByTargetId(
                "multiUser2", LocalDateTime.now().minusDays(1), PageRequest.of(0, 10));
        assertFalse(requests.isEmpty(), "두 번째 알림이 저장되지 않았습니다.");
        
        requests = notificationRequestRepository.findRecentNotificationsByTargetId(
                "multiUser3", LocalDateTime.now().minusDays(1), PageRequest.of(0, 10));
        assertFalse(requests.isEmpty(), "세 번째 알림이 저장되지 않았습니다.");
    }
} 