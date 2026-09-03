package com.badgerevents.interest;

import com.badgerevents.auth.CurrentUserNotFoundException;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventStatus;
import com.badgerevents.user.User;
import com.badgerevents.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventInterestService {

    private final EventInterestRepository eventInterestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventInterestService(
            EventInterestRepository eventInterestRepository,
            UserRepository userRepository,
            EventRepository eventRepository
    ) {
        this.eventInterestRepository = eventInterestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventInterestResponse addInterest(
            Long eventId,
            String currentUserEmail
    ) {
        User user = findCurrentUser(currentUserEmail);
        Event event = findPublishedEvent(eventId);

        boolean alreadyInterested = eventInterestRepository
                .existsByUserIdAndEventId(user.getId(), event.getId());

        if (!alreadyInterested) {
            eventInterestRepository.saveAndFlush(
                    EventInterest.create(user, event)
            );
        }

        return currentState(event.getId(), user.getId());
    }

    @Transactional
    public EventInterestResponse removeInterest(
            Long eventId,
            String currentUserEmail
    ) {
        User user = findCurrentUser(currentUserEmail);
        Event event = findPublishedEvent(eventId);

        eventInterestRepository.deleteByUserIdAndEventId(
                user.getId(),
                event.getId()
        );

        return currentState(event.getId(), user.getId());
    }

    private User findCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(CurrentUserNotFoundException::new);
    }

    private Event findPublishedEvent(Long eventId) {
        return eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"
                ));
    }

    private EventInterestResponse currentState(
            Long eventId,
            Long userId
    ) {
        return new EventInterestResponse(
                eventId,
                eventInterestRepository.countByEventId(eventId),
                eventInterestRepository
                        .existsByUserIdAndEventId(userId, eventId)
        );
    }
}