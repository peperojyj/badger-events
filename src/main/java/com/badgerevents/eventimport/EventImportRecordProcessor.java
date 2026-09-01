package com.badgerevents.eventimport;

import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventImportRecordProcessor {

    private final EventRepository eventRepository;
    private final ImportedEventValidator validator;

    public EventImportRecordProcessor(
            EventRepository eventRepository,
            ImportedEventValidator validator
    ) {
        this.eventRepository = eventRepository;
        this.validator = validator;
    }

    @Transactional
    public ImportAction process(ImportedEventData data) {
        validator.validate(data);

        Event existingEvent = eventRepository
                .findBySourceAndExternalId(
                        data.source(),
                        data.externalId()
                )
                .orElse(null);

        if (existingEvent == null) {
            Event newEvent = Event.createImported(
                    data.source(),
                    data.externalId(),
                    data.title(),
                    data.description(),
                    data.category(),
                    data.startTime(),
                    data.endTime(),
                    data.location(),
                    data.eventUrl()
            );

            eventRepository.save(newEvent);

            return ImportAction.CREATED;
        }

        boolean changed = existingEvent.updateFromImport(
                data.title(),
                data.description(),
                data.category(),
                data.startTime(),
                data.endTime(),
                data.location(),
                data.eventUrl()
        );

        if (!changed) {
            return ImportAction.SKIPPED;
        }

        return ImportAction.UPDATED;
    }
}