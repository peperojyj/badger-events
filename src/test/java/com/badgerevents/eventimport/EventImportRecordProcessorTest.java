package com.badgerevents.eventimport;

import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventImportRecordProcessorTest {

    private static final Instant START_TIME =
            Instant.parse("2026-09-01T15:00:00Z");

    private EventRepository eventRepository;
    private EventImportRecordProcessor processor;
    private ImportedEventValidator validator;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        validator = new ImportedEventValidator();

        processor = new EventImportRecordProcessor(
                eventRepository,
                validator
        );
    }

    @Test
    void createsEventWhenExternalIdentityDoesNotExist() {
        ImportedEventData data =
                importedData("Career Fair");

        when(eventRepository.findBySourceAndExternalId(
                EventSource.UW_EVENTS,
                "222983"
        )).thenReturn(Optional.empty());

        ImportAction action =
                processor.process(data);

        assertThat(action)
                .isEqualTo(ImportAction.CREATED);

        verify(eventRepository)
                .save(any(Event.class));
    }

    @Test
    void updatesEventWhenImportedFieldsChanged() {
        Event existingEvent =
                existingEvent("Old Career Fair");

        ImportedEventData data =
                importedData("Fall Career Fair");

        when(eventRepository.findBySourceAndExternalId(
                EventSource.UW_EVENTS,
                "222983"
        )).thenReturn(Optional.of(existingEvent));

        ImportAction action =
                processor.process(data);

        assertThat(action)
                .isEqualTo(ImportAction.UPDATED);

        assertThat(existingEvent.getTitle())
                .isEqualTo("Fall Career Fair");

        verify(eventRepository, never())
                .save(existingEvent);
    }

    @Test
    void skipsEventWhenImportedFieldsAreUnchanged() {
        Event existingEvent =
                existingEvent("Career Fair");

        ImportedEventData data =
                importedData("Career Fair");

        when(eventRepository.findBySourceAndExternalId(
                EventSource.UW_EVENTS,
                "222983"
        )).thenReturn(Optional.of(existingEvent));

        ImportAction action =
                processor.process(data);

        assertThat(action)
                .isEqualTo(ImportAction.SKIPPED);

        verify(eventRepository, never())
                .save(any(Event.class));
    }

    private ImportedEventData importedData(String title) {
        return new ImportedEventData(
                EventSource.UW_EVENTS,
                "222983",
                title,
                "Career event description",
                "CAREERS AND ADVISING",
                START_TIME,
                null,
                "Union South",
                "https://example.edu/career-fair"
        );
    }

    private Event existingEvent(String title) {
        return Event.createImported(
                EventSource.UW_EVENTS,
                "222983",
                title,
                "Career event description",
                "CAREERS AND ADVISING",
                START_TIME,
                null,
                "Union South",
                "https://example.edu/career-fair"
        );
    }
}