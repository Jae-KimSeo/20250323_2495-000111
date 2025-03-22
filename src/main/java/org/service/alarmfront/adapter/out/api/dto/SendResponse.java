package org.service.alarmfront.adapter.out.api.dto;

import lombok.Data;

@Data
public class SendResponse {
    private String resultCode;
    
    public boolean isSuccess() {
        return "SUCCESS".equals(resultCode);
    }
}