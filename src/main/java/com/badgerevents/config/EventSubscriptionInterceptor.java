package com.badgerevents.config;

import com.badgerevents.event.EventRepository;
import com.badgerevents.event.EventStatus;
import com.badgerevents.chat.ChatMembershipService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.Principal;

@Component
public class EventSubscriptionInterceptor
        //Spring이 제공하는 interface. 이 객체는 Message Channel을 통과하는 메시지를 중간에서 검사할 수 있음
        // destination 형식이 정상인지, event 가 존재하는지, PUBLISHED 상태가 맞는지를 확인
        implements ChannelInterceptor {

    private static final Pattern EVENT_TOPIC = Pattern.compile(
            "^/topic/events/(\\d+)$"
    );

    private final EventRepository eventRepository;
    private final ChatMembershipService chatMembershipService;

    public EventSubscriptionInterceptor(
            EventRepository eventRepository,
            ChatMembershipService chatMembershipService
    ) {
        this.eventRepository = eventRepository;
        this.chatMembershipService = chatMembershipService;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(message);

        if (accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        Matcher matcher = EVENT_TOPIC.matcher(
                String.valueOf(accessor.getDestination())
        );

        if (!matcher.matches()) {
            throw new AccessDeniedException(
                    "Subscription destination is not allowed"
            );
        }

        Long eventId = Long.valueOf(matcher.group(1));

        boolean publishedEventExists = eventRepository
                .findByIdAndStatus(
                        eventId,
                        EventStatus.PUBLISHED
                )
                .isPresent();

        if (!publishedEventExists) {
            throw new AccessDeniedException(
                    "Published event not found"
            );
        }

        Principal principal = accessor.getUser();

        if (principal == null) {
            throw new AccessDeniedException(
                    "Authentication is required"
            );
        }

        chatMembershipService.requireMembership(
                eventId,
                principal.getName()
        );

        return message;
    }
}