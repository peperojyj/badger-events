package com.badgerevents.event;

import com.badgerevents.interest.EventInterestRepository;
import com.badgerevents.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventServiceTest {

    private EventRepository eventRepository;
    private EventInterestRepository eventInterestRepository;
    private UserRepository userRepository;
    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        eventInterestRepository = mock(EventInterestRepository.class);
        userRepository = mock(UserRepository.class);

        eventService = new EventService(
                eventRepository,
                eventInterestRepository,
                userRepository
        );

        when(eventInterestRepository.countByEventIds(any()))
                .thenReturn(List.of());
    }

    @Test
    void returnsUpcomingPublishedEventsWhenKeywordIsBlank() {
        when(eventRepository
                .findAllByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
                        any(EventStatus.class),
                        any(Instant.class)
                ))
                .thenReturn(List.of(event("Career Fair")));

        List<EventSummaryResponse> result = eventService
                .getUpcomingEvents("  ", null);

        assertThat(result)
                .extracting(EventSummaryResponse::title)
                .containsExactly("Career Fair");

        verify(eventRepository, never()).searchUpcomingPublishedEvents(
                any(EventStatus.class),
                any(Instant.class),
                any(String.class)
        );
    }

    @Test
    void trimsKeywordAndUsesSearchQuery() {
        when(eventRepository.searchUpcomingPublishedEvents(
                any(EventStatus.class),
                any(Instant.class),
                any(String.class)
        )).thenReturn(List.of(event("Science Talk")));

        List<EventSummaryResponse> result = eventService
                .getUpcomingEvents("  science  ", null);

        assertThat(result)
                .extracting(EventSummaryResponse::title)
                .containsExactly("Science Talk");

        verify(eventRepository).searchUpcomingPublishedEvents(
                any(EventStatus.class),
                any(Instant.class),
                org.mockito.ArgumentMatchers.eq("science")
        );
    }

    private Event event(String title) {
        return Event.createImported(
                EventSource.UW_EVENTS,
                "day4-" + title,
                title,
                "Event description",
                "ACADEMIC",
                Instant.parse("2026-09-15T18:00:00Z"),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        );
    }
}