package com.badgerevents.event;

import java.time.Instant;

public record EventDetailResponse(
        Long id,
        String title,
        String description,
        String category,
        Instant startTime,
        Instant endTime,
        String location,
        String eventUrl,
        EventStatus status,
        long interestedCount,
        boolean interestedByMe
) {

    public static EventDetailResponse from(
            Event event,
            long interestedCount,
            boolean interestedByMe
    ) {
        return new EventDetailResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getCategory(),
                event.getStartTime(),
                event.getEndTime(),
                event.getLocation(),
                event.getEventUrl(),
                event.getStatus(),
                interestedCount,
                interestedByMe
        );
    }
}