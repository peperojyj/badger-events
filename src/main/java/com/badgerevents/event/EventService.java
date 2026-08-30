package com.badgerevents.event;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getUpcomingEvents() {
        return eventRepository
                .findAllByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        EventStatus.PUBLISHED,
                        Instant.now()
                )
                .stream()
                .map(EventSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<EventDetailResponse> findPublishedEvent(Long eventId) {
        return eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .map(EventDetailResponse::from);
    }
}