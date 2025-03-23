package org.service.alarmfront.adapter.out.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.adapter.out.api.dto.EmailSendRequest;
import org.service.alarmfront.adapter.out.api.dto.SendResponse;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {
    
    private final RestTemplate restTemplate;
    private static final String EMAIL_SEND_URL = "http://localhost:8090/send/email";
    
    @Override
    public boolean send(NotificationRequest request) {
        log.info("이메일 발송 시도: targetId={}, contents={}", request.getTargetId(), request.getContents());
        
        EmailSendRequest emailRequest = EmailSendRequest.builder()
                .emailAddress(request.getTargetId())
                .title("알림")
                .contents(request.getContents())
                .build();
        
        ResponseEntity<SendResponse> response = restTemplate.postForEntity(
                EMAIL_SEND_URL, 
                emailRequest, 
                SendResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String resultCode = response.getBody().getResultCode();
            log.info("이메일 발송 서버 응답: resultCode={}", resultCode);
            return "SUCCESS".equals(resultCode);
        }
        
        log.error("이메일 발송 실패: 응답 상태 코드={}", response.getStatusCodeValue());
        return false;
    }
}