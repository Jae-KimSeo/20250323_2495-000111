package org.service.alarmfront.adapter.out.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SmsSendRequest {
    private String phoneNumber;
    private String title;
    private String contents;
}