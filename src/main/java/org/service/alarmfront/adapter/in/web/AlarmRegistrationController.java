package org.service.alarmfront.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmRegistrationController {

    private final RegisterAlarmUseCase registerAlarmUseCase;
    
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
}