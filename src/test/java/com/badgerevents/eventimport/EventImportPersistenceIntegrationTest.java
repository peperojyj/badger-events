package com.badgerevents.eventimport;

import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventSource;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EventImportPersistenceIntegrationTest {

    @Autowired
    private EventImportRecordProcessor recordProcessor;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void importingSameExternalEventTwiceCreatesThenSkips() {
        ImportedEventData data =
                importedData("Career Fair");

        ImportAction firstAction =
                recordProcessor.process(data);

        entityManager.flush();
        entityManager.clear();

        ImportAction secondAction =
                recordProcessor.process(data);

        entityManager.flush();
        entityManager.clear();

        assertThat(firstAction)
                .isEqualTo(ImportAction.CREATED);

        assertThat(secondAction)
                .isEqualTo(ImportAction.SKIPPED);

        assertThat(
                eventRepository
                        .findBySourceAndExternalId(
                                EventSource.UW_EVENTS,
                                "integration-idempotency-001"
                        )
        ).isPresent();
    }

    @Test
    void importingChangedExternalEventUpdatesManagedEntity() {
        recordProcessor.process(
                importedData("Old Career Fair")
        );

        entityManager.flush();
        entityManager.clear();

        ImportAction action =
                recordProcessor.process(
                        importedData(
                                "Updated Career Fair"
                        )
                );

        entityManager.flush();
        entityManager.clear();

        Event updatedEvent = eventRepository
                .findBySourceAndExternalId(
                        EventSource.UW_EVENTS,
                        "integration-idempotency-001"
                )
                .orElseThrow();

        assertThat(action)
                .isEqualTo(ImportAction.UPDATED);

        assertThat(updatedEvent.getTitle())
                .isEqualTo("Updated Career Fair");
    }

    private ImportedEventData importedData(
            String title
    ) {
        return new ImportedEventData(
                EventSource.UW_EVENTS,
                "integration-idempotency-001",
                title,
                "Integration test event",
                "CAREER",
                Instant.parse("2026-09-10T15:00:00Z"),
                null,
                "Union South",
                "https://example.edu/integration-test"
        );
    }
}