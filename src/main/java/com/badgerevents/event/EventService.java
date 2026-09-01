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
    public List<EventSummaryResponse> getUpcomingEvents(String keyword) {
        String normalizedKeyword =
                keyword == null ? "" : keyword.trim();

        Instant now = Instant.now();

        List<Event> events = normalizedKeyword.isBlank()
                ? eventRepository
                        .findAllByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                                EventStatus.PUBLISHED,
                                now
                        )
                : eventRepository.searchUpcomingPublishedEvents(
                        EventStatus.PUBLISHED,
                        now,
                        normalizedKeyword
                );

        return events
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