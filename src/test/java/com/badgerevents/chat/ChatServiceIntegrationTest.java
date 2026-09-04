package com.badgerevents.chat;

import com.badgerevents.auth.AuthService;
import com.badgerevents.auth.RegisterRequest;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ChatServiceIntegrationTest {

    private static final String EMAIL = "chat@example.com";

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatMembershipService chatMembershipService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AuthService authService;

    private Event event;
    private Event otherEvent;

    @BeforeEach
    void setUp() {
        authService.register(new RegisterRequest(
                EMAIL,
                "password123",
                "Chat Student"
        ));

        event = eventRepository.saveAndFlush(event(
                "chat-event",
                "Chat Event"
        ));
        otherEvent = eventRepository.saveAndFlush(event(
                "other-chat-event",
                "Other Chat Event"
        ));

        chatMembershipService.join(event.getId(), EMAIL);
        chatMembershipService.join(otherEvent.getId(), EMAIL);
    }

    @Test
    void savesMessageUnderTheAuthenticatedUser() {
        ChatMessageResponse response = chatService.saveMessage(
                event.getId(),
                EMAIL,
                "  Is anyone going?  "
        );

        ChatMessage storedMessage = chatMessageRepository
                .findById(response.id())
                .orElseThrow();

        assertThat(response.authorDisplayName())
                .isEqualTo("Chat Student");
        assertThat(response.authorId())
                .isEqualTo(storedMessage.getAuthor().getId());
        assertThat(response.content()).isEqualTo("Is anyone going?");
        assertThat(storedMessage.getAuthor().getEmail()).isEqualTo(EMAIL);
        assertThat(storedMessage.getEvent().getId())
                .isEqualTo(event.getId());
    }

    @Test
    void returnsOnlyTheLatestFiftyMessagesForTheRequestedEvent() {
        chatService.saveMessage(
                otherEvent.getId(),
                EMAIL,
                "Other event message"
        );

        for (int number = 1; number <= 51; number++) {
            chatService.saveMessage(
                    event.getId(),
                    EMAIL,
                    "Message " + number
            );
        }

        List<ChatMessageResponse> messages = chatService
                .getRecentMessages(event.getId(), EMAIL);

        assertThat(messages).hasSize(50);
        assertThat(messages.getFirst().content()).isEqualTo("Message 2");
        assertThat(messages.getLast().content()).isEqualTo("Message 51");
        assertThat(messages)
                .extracting(ChatMessageResponse::eventId)
                .containsOnly(event.getId());
    }

    @Test
    void rejectsBlankAndOversizedMessages() {
        assertThatThrownBy(() -> chatService.saveMessage(
                event.getId(),
                EMAIL,
                "   "
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception)
                                .getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> chatService.saveMessage(
                event.getId(),
                EMAIL,
                "x".repeat(1001)
        )).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void rejectsMessageForANonexistentEvent() {
        assertThatThrownBy(() -> chatService.saveMessage(
                999_999L,
                EMAIL,
                "Hello"
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> assertThat(
                        ((ResponseStatusException) exception)
                                .getStatusCode()
                ).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void rejectsMessageFromAUserWhoDidNotJoin() {
        String otherEmail = "not-joined@example.com";
        authService.register(new RegisterRequest(
                otherEmail,
                "password123",
                "Not Joined Student"
        ));

        assertThatThrownBy(() -> chatService.saveMessage(
                event.getId(),
                otherEmail,
                "I did not join"
        )).isInstanceOf(AccessDeniedException.class);
    }

    private Event event(String externalId, String title) {
        return Event.createImported(
                EventSource.UW_EVENTS,
                externalId,
                title,
                "Event description",
                "SOCIAL",
                Instant.parse("2026-10-10T18:00:00Z"),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        );
    }
}
