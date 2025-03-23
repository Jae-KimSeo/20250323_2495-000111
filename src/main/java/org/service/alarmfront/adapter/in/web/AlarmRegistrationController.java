package org.service.alarmfront.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.service.alarmfront.application.port.out.NotificationHistoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmRegistrationController {

    private final RegisterAlarmUseCase registerAlarmUseCase;
    private final NotificationRequestRepository notificationRequestRepository;
    
    @PostMapping
    public ResponseEntity<AlarmRegistrationResponse> registerAlarm(@RequestBody AlarmRegistrationRequest request) {
        try {
            Long alarmId = registerAlarmUseCase.registerAlarm(request.toCommand());
            return ResponseEntity.ok(AlarmRegistrationResponse.success(alarmId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(AlarmRegistrationResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(AlarmRegistrationResponse.error("알림 등록 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAlarmStatus(@PathVariable Long id) {
        try {
            Optional<NotificationRequest> request = notificationRequestRepository.findById(id);
            if (request.isPresent()) {
                NotificationRequest alarm = request.get();
                Map<String, Object> response = new HashMap<>();
                response.put("alarmId", alarm.getId());
                response.put("status", alarm.getStatus().toString());
                response.put("targetId", alarm.getTargetId());
                response.put("channel", alarm.getChannel().toString());
                response.put("contents", alarm.getContents());
                
                if (alarm.getScheduledTime() != null) {
                    response.put("scheduledTime", alarm.getScheduledTime());
                }
                
                response.put("historyCount", alarm.getHistories().size());
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}