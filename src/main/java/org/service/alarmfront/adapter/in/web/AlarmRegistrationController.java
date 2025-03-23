package org.service.alarmfront.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.service.alarmfront.application.port.out.NotificationRequestRepository;
import org.service.alarmfront.domain.entity.NotificationRequest;

import java.util.Map;
import java.util.Optional;

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
                return ResponseEntity.ok(Map.of(
                    "alarmId", alarm.getId(),
                    "status", alarm.getStatus(),
                    "targetId", alarm.getTargetId(),
                    "channel", alarm.getChannel(),
                    "contents", alarm.getContents(),
                    "scheduledTime", alarm.getScheduledTime(),
                    "historyCount", alarm.getHistories().size()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}