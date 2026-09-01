package com.badgerevents.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
        EventStatus status,
        Instant startTime
    );

    @Query("""
        SELECT event
        FROM Event event
        WHERE event.status = :status
          AND event.startTime >= :startTime
          AND (
                LOWER(event.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(COALESCE(event.description, ''))
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
             OR LOWER(COALESCE(event.location, ''))
                    LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
        ORDER BY event.startTime ASC
        """)
    List<Event> searchUpcomingPublishedEvents(
            @Param("status") EventStatus status,
            @Param("startTime") Instant startTime,
            @Param("keyword") String keyword
    );

    Optional<Event> findByIdAndStatus(Long id, EventStatus status);

    Optional<Event> findBySourceAndExternalId(
        EventSource source,
        String externalId
    );


}
