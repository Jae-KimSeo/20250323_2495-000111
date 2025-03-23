package org.service.alarmfront.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.service.alarmfront.config.TestConfig;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ClientType;
import org.service.alarmfront.domain.value.Status;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ActiveProfiles("test")
public class AlarmRegistrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRequestRepository notificationRequestRepository;

    @BeforeEach
    void setUp() {
        TestConfig.resetLatch(1);
    }

    @Test
    void registerAlarm_MobileClient_Success() throws Exception {
        AlarmRegistrationRequest request = new AlarmRegistrationRequest();
        request.setClientType("MOBILE");
        request.setAlarmType("alert");
        request.setTargetId("user123");
        request.setContent("테스트 알림 메시지");
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("preferredChannel", "SMS");
        request.setAdditionalData(additionalData);

        mockMvc.perform(post("/api/alarms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.alarmId", notNullValue()));

        boolean processed = TestConfig.alarmProcessingLatch.await(5, TimeUnit.SECONDS);
        assertTrue(processed, "알림 처리가 시간 내에 완료되지 않았습니다.");

        Page<NotificationRequest> requestPage = notificationRequestRepository.findRecentNotificationsByTargetId(
                "user123", LocalDateTime.now().minusDays(1), PageRequest.of(0, 10));
        
        assertTrue(!requestPage.isEmpty(), "알림 요청이 저장되지 않았습니다.");
        NotificationRequest savedRequest = requestPage.getContent().get(0);
        assertEquals(Status.COMPLETED, savedRequest.getStatus(), "알림 상태가 올바르게 업데이트되지 않았습니다.");
        assertEquals(Channel.SMS, savedRequest.getChannel(), "알림 채널이 올바르게 설정되지 않았습니다.");
    }

    @Test
    void registerAlarm_WebClient_ScheduledSuccess() throws Exception {
        LocalDateTime scheduledTime = LocalDateTime.now().plusHours(1);
        String formattedTime = scheduledTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        
        AlarmRegistrationRequest request = new AlarmRegistrationRequest();
        request.setClientType("WEB");
        request.setAlarmType("reminder");
        request.setTargetId("user456");
        request.setContent("예약 알림 테스트");
        request.setScheduledTime(formattedTime);

        mockMvc.perform(post("/api/alarms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.alarmId", notNullValue()));

        TimeUnit.SECONDS.sleep(1);

        Page<NotificationRequest> requestPage = notificationRequestRepository.findRecentNotificationsByTargetId(
                "user456", LocalDateTime.now().minusDays(1), PageRequest.of(0, 10));
        
        assertTrue(!requestPage.isEmpty(), "알림 요청이 저장되지 않았습니다.");
        NotificationRequest savedRequest = requestPage.getContent().get(0);
        assertEquals(Status.SCHEDULED, savedRequest.getStatus(), "예약 알림의 상태가 올바르게 설정되지 않았습니다.");
        assertEquals(Channel.EMAIL, savedRequest.getChannel(), "알림 채널이 올바르게 설정되지 않았습니다.");
    }

    @Test
    void registerAlarm_InvalidRequest_BadRequest() throws Exception {
        AlarmRegistrationRequest request = new AlarmRegistrationRequest();
        request.setClientType("MOBILE");
        request.setAlarmType("alert");
        request.setContent("테스트 알림 메시지");

        mockMvc.perform(post("/api/alarms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("ERROR")))
                .andExpect(jsonPath("$.message", containsString("대상 ID")));
    }

    @Test
    void getNotificationHistory_Success() throws Exception {
        String customerId = "testUser";
        NotificationRequest request = NotificationRequest.create(
                customerId,
                Channel.SMS,
                "테스트 알림 내용",
                null
        );
        request.updateStatus(Status.COMPLETED);
        notificationRequestRepository.save(request);

        mockMvc.perform(get("/api/alarms")
                .param("customerId", customerId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].targetId", is(customerId)))
                .andExpect(jsonPath("$.content[0].channel", is("SMS")))
                .andExpect(jsonPath("$.content[0].status", is("COMPLETED")));
    }

    @Test
    void getNotificationHistory_EmptyResult() throws Exception {
        String nonExistingCustomerId = "nonExistingUser";

        mockMvc.perform(get("/api/alarms")
                .param("customerId", nonExistingCustomerId)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty", is(true)));
    }
} 