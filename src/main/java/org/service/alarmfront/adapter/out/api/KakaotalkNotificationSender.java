package org.service.alarmfront.adapter.out.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.adapter.out.api.dto.KakaotalkSendRequest;
import org.service.alarmfront.adapter.out.api.dto.SendResponse;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaotalkNotificationSender implements NotificationSender {
    
    private final RestTemplate restTemplate;
    private static final String KAKAOTALK_SEND_URL = "http://localhost:8090/send/kakaotalk";
    
    @Override
    public boolean send(NotificationRequest request) {
        log.info("카카오톡 발송 시도: targetId={}, contents={}", request.getTargetId(), request.getContents());
        
        KakaotalkSendRequest kakaotalkRequest = KakaotalkSendRequest.builder()
                .talkId(request.getTargetId())
                .title("알림")
                .contents(request.getContents())
                .build();
        
        ResponseEntity<SendResponse> response = restTemplate.postForEntity(
                KAKAOTALK_SEND_URL, 
                kakaotalkRequest, 
                SendResponse.class
        );
        
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String resultCode = response.getBody().getResultCode();
            log.info("카카오톡 발송 서버 응답: resultCode={}", resultCode);
            return "SUCCESS".equals(resultCode);
        }
        
        log.error("카카오톡 발송 실패: 응답 상태 코드={}", response.getStatusCodeValue());
        return false;
    }
}