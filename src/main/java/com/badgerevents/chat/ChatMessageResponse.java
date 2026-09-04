package com.badgerevents.chat;

import java.time.Instant;

public record ChatMessageResponse(
        Long id,
        Long eventId,
        Long authorId,
        String authorDisplayName,
        String content,
        Instant createdAt
) {

    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getEvent().getId(),
                message.getAuthor().getId(),
                message.getAuthor().getDisplayName(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}