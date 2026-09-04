package com.badgerevents.chat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT message
            FROM ChatMessage message
            JOIN FETCH message.author
            WHERE message.event.id = :eventId
            ORDER BY message.createdAt DESC, message.id DESC
            """)
    List<ChatMessage> findRecentByEventId(
            @Param("eventId") Long eventId,
            Pageable pageable
    );
}