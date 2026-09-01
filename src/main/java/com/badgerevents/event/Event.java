package com.badgerevents.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;


@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length= 50)
    private EventSource source;

    @Column(name = "external_id", nullable = false, length = 255)
    private String externalId;


    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String category;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(length = 255)
    private String location;

    @Column(name = "event_url", length = 1000)
    private String eventUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Event() {
    }

    private Event(
            EventSource source,
            String externalId,
            String title,
            String description,
            String category,
            Instant startTime,
            Instant endTime,
            String location,
            String eventUrl
    ) {
        this.source = Objects.requireNonNull(source);
        this.externalId = Objects.requireNonNull(externalId);
        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.category = category;
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = endTime;
        this.location = location;
        this.eventUrl = eventUrl;
        this.status = EventStatus.PUBLISHED;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Event createImported(
            EventSource source,
            String externalId,
            String title,
            String description,
            String category,
            Instant startTime,
            Instant endTime,
            String location,
            String eventUrl
    ) {
        return new Event(
                source,
                externalId,
                title,
                description,
                category,
                startTime,
                endTime,
                location,
                eventUrl
        );
    }

    public boolean updateFromImport(
            String title,
            String description,
            String category,
            Instant startTime,
            Instant endTime,
            String location,
            String eventUrl
    ) {
        boolean changed = !Objects.equals(this.title, title)
                || !Objects.equals(this.description, description)
                || !Objects.equals(this.category, category)
                || !Objects.equals(this.startTime, startTime)
                || !Objects.equals(this.endTime, endTime)
                || !Objects.equals(this.location, location)
                || !Objects.equals(this.eventUrl, eventUrl);

        if (!changed) {
            return false;
        }

        this.title = Objects.requireNonNull(title);
        this.description = description;
        this.category = category;
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = endTime;
        this.location = location;
        this.eventUrl = eventUrl;
        this.updatedAt = Instant.now();

        return true;
    }

    public Long getId() {
        return id;
    }

    public EventSource getSource() {
        return source;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public String getEventUrl() {
        return eventUrl;
    }

    public EventStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
