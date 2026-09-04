package com.badgerevents.config;

import com.badgerevents.chat.ChatMembershipService;
import com.badgerevents.event.Event;
import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventSource;
import com.badgerevents.event.EventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventSubscriptionInterceptorTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private MessageChannel messageChannel;

    @Mock
    private ChatMembershipService chatMembershipService;

    @Test
    void allowsSubscriptionToAPublishedEventTopic() {
        EventSubscriptionInterceptor interceptor =
                new EventSubscriptionInterceptor(
                        eventRepository,
                        chatMembershipService
                );
        Event event = Event.createImported(
                EventSource.UW_EVENTS,
                "subscription-event",
                "Subscription Event",
                "Description",
                "SOCIAL",
                Instant.parse("2026-10-10T18:00:00Z"),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        );
        when(eventRepository.findByIdAndStatus(
                36L,
                EventStatus.PUBLISHED
        )).thenReturn(Optional.of(event));

        Message<byte[]> message = subscribeMessage(
                "/topic/events/36"
        );

        Message<?> result = interceptor.preSend(message, messageChannel);

        assertThat(result).isSameAs(message);
        verify(chatMembershipService).requireMembership(
                36L,
                "chat@example.com"
        );
    }

    @Test
    void rejectsSubscriptionToANonexistentEvent() {
        EventSubscriptionInterceptor interceptor =
                new EventSubscriptionInterceptor(
                        eventRepository,
                        chatMembershipService
                );
        when(eventRepository.findByIdAndStatus(
                999_999L,
                EventStatus.PUBLISHED
        )).thenReturn(Optional.empty());

        Message<byte[]> message = subscribeMessage(
                "/topic/events/999999"
        );

        assertThatThrownBy(() -> interceptor.preSend(
                message,
                messageChannel
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsUnexpectedSubscriptionDestination() {
        EventSubscriptionInterceptor interceptor =
                new EventSubscriptionInterceptor(
                        eventRepository,
                        chatMembershipService
                );

        Message<byte[]> message = subscribeMessage("/topic/admin");

        assertThatThrownBy(() -> interceptor.preSend(
                message,
                messageChannel
        )).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void rejectsSubscriptionFromAUserWhoDidNotJoin() {
        EventSubscriptionInterceptor interceptor =
                new EventSubscriptionInterceptor(
                        eventRepository,
                        chatMembershipService
                );
        Event event = Event.createImported(
                EventSource.UW_EVENTS,
                "members-only-event",
                "Members Only Event",
                "Description",
                "SOCIAL",
                Instant.parse("2026-10-10T18:00:00Z"),
                null,
                "Memorial Union",
                "https://today.wisc.edu"
        );
        when(eventRepository.findByIdAndStatus(
                36L,
                EventStatus.PUBLISHED
        )).thenReturn(Optional.of(event));
        doThrow(new AccessDeniedException("Join first"))
                .when(chatMembershipService)
                .requireMembership(36L, "chat@example.com");

        Message<byte[]> message = subscribeMessage(
                "/topic/events/36"
        );

        assertThatThrownBy(() -> interceptor.preSend(
                message,
                messageChannel
        )).isInstanceOf(AccessDeniedException.class);
    }

    private Message<byte[]> subscribeMessage(String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(
                StompCommand.SUBSCRIBE
        );
        accessor.setDestination(destination);
        accessor.setUser(() -> "chat@example.com");

        return MessageBuilder.createMessage(
                new byte[0],
                accessor.getMessageHeaders()
        );
    }
}
