package org.service.alarmfront.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.service.alarmfront.adapter.in.web.NotificationHistoryResponseDTO;
import org.service.alarmfront.domain.entity.NotificationHistory;
import org.service.alarmfront.domain.entity.NotificationRequest;
import org.service.alarmfront.domain.value.Channel;
import org.service.alarmfront.domain.value.ResultCode;
import org.service.alarmfront.domain.value.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
    "logging.level.org.hibernate.SQL=DEBUG",
    "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
public class NotificationHistoryQueryTest {

    @Autowired
    private JpaNotificationRequestRepository jpaNotificationRequestRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void setupTestData() {
        String customerId = "testCustomer";
        for (int i = 0; i < 5; i++) {
            NotificationRequest request = NotificationRequest.create(
                    customerId,
                    Channel.SMS,
                    "Test content " + i,
                    LocalDateTime.now()
            );
            request.updateStatus(Status.COMPLETED);

            entityManager.persist(request);
            entityManager.flush();

            NotificationHistory history = NotificationHistory.createSuccessHistory(request, 1);
            request.addHistory(history);
            
            entityManager.persist(request);
        }
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void testOptimizedQueryFetchesHistoryWithoutN1Problem() {
        setupTestData();
        
        Page<NotificationHistoryResponseDTO> results = jpaNotificationRequestRepository.findNotificationHistoryByTargetId(
                "testCustomer",
                LocalDateTime.now().minusMonths(3), 
                PageRequest.of(0, 10)
        );

        assertThat(results).isNotEmpty();
        assertThat(results.getContent().size()).isGreaterThanOrEqualTo(1);

        for (NotificationHistoryResponseDTO dto : results.getContent()) {
            assertThat(dto.getLatestResultCode()).isEqualTo(ResultCode.SUCCESS);
        }
    }
}
