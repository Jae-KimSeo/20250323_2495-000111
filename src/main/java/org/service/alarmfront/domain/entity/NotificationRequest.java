package org.service.alarmfront.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notification_requests")
@Getter
@NoArgsConstructor
public class NotificationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    private String contents;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationHistory> histories = new ArrayList<>();

    public static NotificationRequest create(Channel channel, String contents, LocalDateTime scheduledTime) {
        NotificationRequest request = new NotificationRequest();
        request.channel = channel;
        request.contents = contents;
        request.scheduledTime = scheduledTime;
        request.status = Status.INIT;
        return request;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void addHistory(NotificationHistory history) {
        this.histories.add(history);
    }

    public int getAttemptCount() {
        return histories.size();
    }
} 