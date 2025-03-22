package org.service.alarmfront.adapter.in.web;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmRegistrationResponse {
    private Long alarmId;
    private String status;
    private String message;
    
    public static AlarmRegistrationResponse success(Long alarmId) {
        return AlarmRegistrationResponse.builder()
                .alarmId(alarmId)
                .status("SUCCESS")
                .message("알림이 성공적으로 등록되었습니다.")
                .build();
    }
    
    public static AlarmRegistrationResponse error(String message) {
        return AlarmRegistrationResponse.builder()
                .status("ERROR")
                .message(message)
                .build();
    }
}