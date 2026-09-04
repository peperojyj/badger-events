package com.badgerevents.chat;

import com.badgerevents.auth.CurrentUserNotFoundException;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventStatus;
import com.badgerevents.user.User;
import com.badgerevents.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ChatService {

    private static final int RECENT_MESSAGE_LIMIT = 50;
    private static final int MAX_CONTENT_LENGTH = 1000;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatMembershipService chatMembershipService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ChatService(
        ChatMessageRepository chatMessageRepository,
        ChatMembershipService chatMembershipService,
        EventRepository eventRepository,
        UserRepository userRepository
    ) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatMembershipService = chatMembershipService;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentMessages(
            Long eventId,
            String currentUserEmail
    ) {
        Event event = findPublishedEvent(eventId);

        chatMembershipService.requireMembership(
                event.getId(),
                currentUserEmail
        );

        List<ChatMessage> newestFirst = chatMessageRepository
                .findRecentByEventId(
                        event.getId(),
                        PageRequest.of(0, RECENT_MESSAGE_LIMIT)
                );

        List<ChatMessage> chronological =
                new ArrayList<>(newestFirst);

        Collections.reverse(chronological);

        return chronological.stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse saveMessage(
            Long eventId,
            String currentUserEmail,
            String content
    ) {
        Event event = findPublishedEvent(eventId);

        User author = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(CurrentUserNotFoundException::new);

        chatMembershipService.requireMembership(
                event.getId(),
                currentUserEmail
        );

        String normalizedContent = normalizeContent(content);

        ChatMessage savedMessage = chatMessageRepository.saveAndFlush(
                ChatMessage.create(event, author, normalizedContent)
        );

        return ChatMessageResponse.from(savedMessage);
    }

    private Event findPublishedEvent(Long eventId) {
        return eventRepository
                .findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Event not found"
                ));
    }

    private String normalizeContent(String content) {
        if (content == null) {
            throw invalidContent();
        }

        String normalized = content.trim();

        if (normalized.isEmpty()
                || normalized.length() > MAX_CONTENT_LENGTH) {
            throw invalidContent();
        }

        return normalized;
    }

    private ResponseStatusException invalidContent() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Message content must be between 1 and 1000 characters"
        );
    }
}