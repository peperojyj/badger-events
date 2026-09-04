package com.badgerevents.chat;

import com.badgerevents.event.Event;
import com.badgerevents.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ChatMessage() {
    }

    private ChatMessage(Event event, User author, String content) {
        this.event = Objects.requireNonNull(event);
        this.author = Objects.requireNonNull(author);
        this.content = Objects.requireNonNull(content);
        this.createdAt = Instant.now();
    }

    public static ChatMessage create(
            Event event,
            User author,
            String content
    ) {
        return new ChatMessage(event, author, content);
    }

    public Long getId() {
        return id;
    }

    public Event getEvent() {
        return event;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}