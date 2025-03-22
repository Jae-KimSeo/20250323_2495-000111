package org.service.alarmfront.adapter.out.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaotalkSendRequest {
    private String talkId;
    private String title;
    private String contents;
}