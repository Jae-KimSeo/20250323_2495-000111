package org.service.alarmfront.adapter.in.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.alarmfront.application.port.in.InquireNotificationHistoryUseCase;
import org.service.alarmfront.application.port.in.RegisterAlarmUseCase;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmRegistrationController {

    private final RegisterAlarmUseCase registerAlarmUseCase;
    private final InquireNotificationHistoryUseCase inquireNotificationHistoryUseCase;
    
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
    
    @GetMapping
    public ResponseEntity<PageResponse<NotificationHistoryResponse>> getNotificationHistory(
            @RequestParam String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("Received request to get notification history for customer: {}", customerId);
            Page<NotificationHistoryResponseDTO> notificationPage = inquireNotificationHistoryUseCase.getNotificationHistory(
                    customerId, page, size);
            
            Page<NotificationHistoryResponse> responsePage = notificationPage.map(NotificationHistoryResponseDTO::toResponse);
            
            return ResponseEntity.ok(PageResponse.from(responsePage));
        } catch (Exception e) {
            log.error("Error retrieving notification history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}