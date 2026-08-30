package com.badgerevents.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStatusAndStartTimeGreaterThanEqualOrderByStartTimeAsc(
        EventStatus status,
        Instant startTime
    );

    Optional<Event> findByIdAndStatus(Long id, EventStatus status);

    Optional<Event> findBySourceAndExternalId(
        EventSource source,
        String externalId
    );
}
