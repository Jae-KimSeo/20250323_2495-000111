package org.service.alarmfront.adapter.out.api;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.out.NotificationSender;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaotalkNotificationSender implements NotificationSender {

    private final RestTemplate restTemplate;
    
    @Override
    public boolean send(NotificationRequest request) {
        String url = "http://localhost:8090/send/kakaotalk";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("talkId", request.getTargetId());
        requestBody.put("title", "알림");
        requestBody.put("contents", request.getContents());
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestBody, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String resultCode = (String) response.getBody().get("resultCode");

                if ("SUCCESS".equals(resultCode)) {
                    request.addHistory(NotificationHistory.createSuccessHistory(request, request.getAttemptCount() + 1));
                    return true;
                } else {
                    request.addHistory(NotificationHistory.createFailHistory(request, "카카오톡 발송 실패: " + resultCode, request.getAttemptCount() + 1));
                    return false;
                }
            } else {
                request.addHistory(NotificationHistory.createFailHistory(request, "카카오톡 발송 API 응답 오류", request.getAttemptCount() + 1));
                return false;
            }
        } catch (Exception e) {
            request.addHistory(NotificationHistory.createFailHistory(request, "카카오톡 발송 중 예외 발생: " + e.getMessage(), request.getAttemptCount() + 1));
            return false;
        }
    }
    
    @Override
    public String checkStatus(Long requestId) {
        return "SENT";
    }
}