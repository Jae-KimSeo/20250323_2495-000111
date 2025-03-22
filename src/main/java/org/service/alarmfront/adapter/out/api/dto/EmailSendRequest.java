package org.service.alarmfront.adapter.out.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailSendRequest {
    private String emailAddress;
    private String title;
    private String contents;
}