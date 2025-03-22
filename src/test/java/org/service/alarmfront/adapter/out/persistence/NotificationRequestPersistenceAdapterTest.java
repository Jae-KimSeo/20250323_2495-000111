package org.service.alarmfront.adapter.out.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.Status;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(NotificationRequestPersistenceAdapter.class)
@ActiveProfiles("test")
class NotificationRequestPersistenceAdapterTest {

    @Autowired
    private NotificationRequestPersistenceAdapter adapter;

    @Test
    void save_test() {
        // given
        LocalDateTime now = LocalDateTime.now();
        NotificationRequest request = NotificationRequest.create(
                Channel.SMS,
                "SMS save test",
                now
        );

        // when
        NotificationRequest savedRequest = adapter.save(request);

        // then
        assertThat(savedRequest).isNotNull();
        assertThat(savedRequest.getId()).isNotNull();
        assertThat(savedRequest.getChannel()).isEqualTo(Channel.SMS);
        assertThat(savedRequest.getContents()).isEqualTo("SMS save test");
        assertThat(savedRequest.getScheduledTime()).isEqualTo(now);
        assertThat(savedRequest.getStatus()).isEqualTo(Status.INIT);
    }

    @Test
    void change_status_and_get_test() {
        // given
        NotificationRequest request1 = NotificationRequest.create(
                Channel.SMS,
                "test msg 1",
                LocalDateTime.now()
        );

        NotificationRequest request2 = NotificationRequest.create(
                Channel.EMAIL,
                "test msg 2",
                LocalDateTime.now()
        );
        request2.updateStatus(Status.QUEUED);

        NotificationRequest request3 = NotificationRequest.create(
                Channel.KAKAOTALK,
                "test msg 3",
                LocalDateTime.now()
        );

        adapter.save(request1);
        adapter.save(request2);
        adapter.save(request3);

        // when
        List<NotificationRequest> initRequests = adapter.findByStatus(Status.INIT);
        List<NotificationRequest> queuedRequests = adapter.findByStatus(Status.QUEUED);

        // then
        assertThat(initRequests).hasSize(2);
        assertThat(queuedRequests).hasSize(1);

        assertThat(initRequests).extracting(NotificationRequest::getContents)
                .containsExactlyInAnyOrder("test msg 1", "test msg 3");

        assertThat(queuedRequests).extracting(NotificationRequest::getContents)
                .containsExactly("test msg 2");
    }
} 