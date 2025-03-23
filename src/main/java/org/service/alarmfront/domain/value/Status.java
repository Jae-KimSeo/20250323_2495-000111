package org.service.alarmfront.domain.value;

public enum Status {
    INIT,
    QUEUED,
    PROCESSING,
    SCHEDULED,
    COMPLETED,
    FAILED,
    RETRY_EXHAUSTED
} 