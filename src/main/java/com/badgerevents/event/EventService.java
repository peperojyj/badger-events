package com.badgerevents.event;

import com.badgerevents.auth.CurrentUserNotFoundException;
import com.badgerevents.interest.EventInterestCount;
import com.badgerevents.interest.EventInterestRepository;
import com.badgerevents.user.User;
import com.badgerevents.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventInterestRepository eventInterestRepository;
    private final UserRepository userRepository;

    public EventService(
            EventRepository eventRepository,
            EventInterestRepository eventInterestRepository,
            UserRepository userRepository
    ) {
        this.eventRepository = eventRepository;
        this.eventInterestRepository = eventInterestRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> getUpcomingEvents(
            String keyword,
            String currentUserEmail
    ) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
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

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Long> interestCounts = getInterestCounts(eventIds);
        Set<Long> interestedEventIds = getInterestedEventIds(
                currentUserEmail,
                eventIds
        );

        return events
                .stream()
                .map(event -> EventSummaryResponse.from(
                        event,
                        interestCounts.getOrDefault(event.getId(), 0L),
                        interestedEventIds.contains(event.getId())
                ))
                .sorted(Comparator
                        .comparing(EventSummaryResponse::interestedByMe)
                        .reversed()
                        .thenComparing(EventSummaryResponse::startTime))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<EventDetailResponse> findPublishedEvent(
            Long eventId,
            String currentUserEmail
    ) {
        return eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .map(event -> EventDetailResponse.from(
                        event,
                        eventInterestRepository.countByEventId(eventId),
                        isInterestedByCurrentUser(
                                currentUserEmail,
                                eventId
                        )
                ));
    }

    private Map<Long, Long> getInterestCounts(List<Long> eventIds) {
        if (eventIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();

        for (EventInterestCount count
                : eventInterestRepository.countByEventIds(eventIds)) {
            result.put(count.eventId(), count.interestedCount());
        }

        return result;
    }

    private Set<Long> getInterestedEventIds(
            String currentUserEmail,
            List<Long> eventIds
    ) {
        if (currentUserEmail == null || eventIds.isEmpty()) {
            return Collections.emptySet();
        }

        User user = findCurrentUser(currentUserEmail);

        return new HashSet<>(
                eventInterestRepository.findInterestedEventIds(
                        user.getId(),
                        eventIds
                )
        );
    }

    private boolean isInterestedByCurrentUser(
            String currentUserEmail,
            Long eventId
    ) {
        if (currentUserEmail == null) {
            return false;
        }

        User user = findCurrentUser(currentUserEmail);

        return eventInterestRepository.existsByUserIdAndEventId(
                user.getId(),
                eventId
        );
    }

    private User findCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(CurrentUserNotFoundException::new);
    }
}