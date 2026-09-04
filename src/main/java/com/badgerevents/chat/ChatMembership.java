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
@Table(name = "chat_memberships")
public class ChatMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected ChatMembership() {
    }

    private ChatMembership(User user, Event event) {
        this.user = Objects.requireNonNull(user);
        this.event = Objects.requireNonNull(event);
        this.joinedAt = Instant.now();
    }

    public static ChatMembership join(User user, Event event) {
        return new ChatMembership(user, event);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }
}