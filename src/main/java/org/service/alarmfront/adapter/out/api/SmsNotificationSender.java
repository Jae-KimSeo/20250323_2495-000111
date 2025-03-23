package org.service.alarmfront.adapter.out.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.adapter.out.api.dto.SendResponse;
import org.service.alarmfront.adapter.out.api.dto.SmsSendRequest;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationSender implements NotificationSender {
    
    private final RestTemplate restTemplate;
    private static final String SMS_SEND_URL = "http://localhost:8090/send/sms";
    
    @Override
    public boolean send(NotificationRequest request) {
        log.info("SMS 발송 시도: targetId={}, contents={}", request.getTargetId(), request.getContents());
        
        SmsSendRequest smsRequest = SmsSendRequest.builder()
                .phoneNumber(request.getTargetId())
                .title("알림")
                .contents(request.getContents())
                .build();
        
        ResponseEntity<SendResponse> response = restTemplate.postForEntity(
                SMS_SEND_URL, 
                smsRequest, 
                SendResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String resultCode = response.getBody().getResultCode();
            log.info("SMS 발송 서버 응답: resultCode={}", resultCode);
            return "SUCCESS".equals(resultCode);
        }
        
        log.error("SMS 발송 실패: 응답 상태 코드={}", response.getStatusCodeValue());
        return false;
    }
}