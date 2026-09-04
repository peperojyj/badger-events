package com.badgerevents.chat;

import com.badgerevents.auth.CurrentUserNotFoundException;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventStatus;
import com.badgerevents.user.User;
import com.badgerevents.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChatMembershipService {

    private final ChatMembershipRepository chatMembershipRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ChatMembershipService(
            ChatMembershipRepository chatMembershipRepository,
            EventRepository eventRepository,
            UserRepository userRepository
    ) {
        this.chatMembershipRepository = chatMembershipRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ChatMembershipResponse getMembership(
            Long eventId,
            String currentUserEmail
    ) {
        Event event = findPublishedEvent(eventId);
        User user = findCurrentUser(currentUserEmail);

        long memberCount = chatMembershipRepository
                .countByEventId(event.getId());

        return chatMembershipRepository
                .findByUserIdAndEventId(user.getId(), event.getId())
                .map(membership -> ChatMembershipResponse.from(
                        membership,
                        memberCount
                ))
                .orElseGet(() -> ChatMembershipResponse.notJoined(
                        memberCount
                ));
    }

    @Transactional
    public ChatMembershipResponse join(
            Long eventId,
            String currentUserEmail
    ) {
        Event event = findPublishedEvent(eventId);
        User user = findCurrentUser(currentUserEmail);

        ChatMembership membership = chatMembershipRepository
                .findByUserIdAndEventId(user.getId(), event.getId())
                .orElseGet(() -> chatMembershipRepository.saveAndFlush(
                        ChatMembership.join(user, event)
                ));

        long memberCount = chatMembershipRepository
                .countByEventId(event.getId());

        return ChatMembershipResponse.from(
                membership,
                memberCount
        );
    }

    @Transactional
    public ChatMembershipResponse leave(
            Long eventId,
            String currentUserEmail
    ) {
        Event event = findPublishedEvent(eventId);
        User user = findCurrentUser(currentUserEmail);

        chatMembershipRepository
                .findByUserIdAndEventId(user.getId(), event.getId())
                .ifPresent(chatMembershipRepository::delete);

        chatMembershipRepository.flush();

        long memberCount = chatMembershipRepository
                .countByEventId(event.getId());

        return ChatMembershipResponse.notJoined(memberCount);
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResponse> getMembers(Long eventId) {
        Event event = findPublishedEvent(eventId);

        return chatMembershipRepository
                .findMembersByEventId(event.getId())
                .stream()
                .map(ChatMemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public void requireMembership(
            Long eventId,
            String currentUserEmail
    ) {
        boolean joined = chatMembershipRepository
                .existsByUserEmailAndEventId(
                        currentUserEmail,
                        eventId
                );

        if (!joined) {
            throw new AccessDeniedException(
                    "Join this event conversation first"
            );
        }
    }

    private Event findPublishedEvent(Long eventId) {
        return eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"
                ));
    }

    private User findCurrentUser(String currentUserEmail) {
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(CurrentUserNotFoundException::new);
    }
}