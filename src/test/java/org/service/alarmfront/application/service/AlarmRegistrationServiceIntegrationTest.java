package org.service.alarmfront.application.service;

import org.junit.jupiter.api.Test;
import org.service.alarmfront.application.port.in.AlarmCommand;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ClientType;
import org.service.alarmfront.domain.value.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AlarmRegistrationServiceIntegrationTest {

    @Autowired
    private AlarmRegistrationService alarmRegistrationService;
    
    @Autowired
    private NotificationRequestRepository notificationRequestRepository;

    @Test
    void registerAlarm_MobileClient_Success() {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("preferredChannel", "SMS");
        
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.MOBILE)
                .alarmType("alert")
                .targetId("user123")
                .content("서비스 테스트 알림")
                .additionalData(additionalData)
                .build();

        Long alarmId = alarmRegistrationService.registerAlarm(command);

        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest savedRequest = optionalRequest.get();
        assertEquals(Status.INIT, savedRequest.getStatus(), "초기 알림 상태가 올바르게 설정되지 않았습니다.");
        assertEquals(Channel.SMS, savedRequest.getChannel(), "알림 채널이 올바르게 설정되지 않았습니다.");
        assertEquals("user123", savedRequest.getTargetId(), "알림 대상 ID가 올바르게 설정되지 않았습니다.");
    }

    @Test
    void registerAlarm_SystemClient_Success() {
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.SYSTEM)
                .alarmType("notification")
                .targetId("user123")
                .content("시스템 알림 테스트")
                .build();

        Long alarmId = alarmRegistrationService.registerAlarm(command);

        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest savedRequest = optionalRequest.get();
        assertEquals(Status.INIT, savedRequest.getStatus(), "초기 알림 상태가 올바르게 설정되지 않았습니다.");
        assertEquals(Channel.KAKAOTALK, savedRequest.getChannel(), "시스템 알림 타입에 따른 채널이 올바르게 설정되지 않았습니다.");
    }

    @Test
    void processAlarmRecord_ImmediateSending_StatusChangeSuccess() throws InterruptedException {
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.WEB)
                .targetId("user123")
                .content("알림 처리 테스트")
                .build();
        
        Long alarmId = alarmRegistrationService.registerAlarm(command);
        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        alarmRegistrationService.processAlarmRecord(alarmId);

        TimeUnit.SECONDS.sleep(1);

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest processedRequest = optionalRequest.get();
        assertEquals(Status.COMPLETED, processedRequest.getStatus(), "처리 후 알림 상태가 올바르게 업데이트되지 않았습니다.");
        assertEquals(1, processedRequest.getHistories().size(), "알림 처리 히스토리가 생성되지 않았습니다.");
    }

    @Test
    void processAlarmRecord_ScheduledSending_StatusChangeSuccess() {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        
        AlarmCommand command = AlarmCommand.builder()
                .clientType(ClientType.WEB)
                .targetId("user123")
                .content("예약 알림 처리 테스트")
                .scheduledTime(scheduledTime)
                .build();
        
        Long alarmId = alarmRegistrationService.registerAlarm(command);
        assertNotNull(alarmId, "알림 ID가 생성되지 않았습니다.");

        alarmRegistrationService.processAlarmRecord(alarmId);

        Optional<NotificationRequest> optionalRequest = notificationRequestRepository.findById(alarmId);
        assertTrue(optionalRequest.isPresent(), "알림 요청이 저장되지 않았습니다.");
        
        NotificationRequest processedRequest = optionalRequest.get();
        assertEquals(Status.SCHEDULED, processedRequest.getStatus(), "예약 알림 상태가 올바르게 설정되지 않았습니다.");
        assertEquals(scheduledTime, processedRequest.getScheduledTime(), "예약 시간이 올바르게 설정되지 않았습니다.");
    }

    @Test
    void registerAlarm_InvalidRequest_ExceptionThrown() {
        AlarmCommand invalidCommand = AlarmCommand.builder()
                .clientType(ClientType.MOBILE)
                .alarmType("alert")
                .content("유효하지 않은 알림 테스트")
                .build();

        assertThrows(IllegalArgumentException.class, 
                () -> alarmRegistrationService.registerAlarm(invalidCommand),
                "필수 필드 누락 시 예외가 발생해야 합니다.");
    }
} 