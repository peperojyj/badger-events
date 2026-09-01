package com.badgerevents.eventimport;

import com.badgerevents.event.EventSource;

import java.time.Instant;

public record ImportedEventData(
        EventSource source,
        String externalId,
        String title,
        String description,
        String category,
        Instant startTime,
        Instant endTime,
        String location,
        String eventUrl
) {
}