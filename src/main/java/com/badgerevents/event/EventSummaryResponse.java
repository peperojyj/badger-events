package com.badgerevents.event;

import java.time.Instant;

public record EventSummaryResponse(
        Long id,
        String title,
        String category,
        Instant startTime,
        Instant endTime,
        String location,
        EventStatus status,
        long interestedCount,
        boolean interestedByMe
) {

    public static EventSummaryResponse from(
            Event event,
            long interestedCount,
            boolean interestedByMe
    ) {
        return new EventSummaryResponse(
                event.getId(),
                event.getTitle(),
                event.getCategory(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getStatus(),
                interestedCount,
                interestedByMe
        );
    }
}