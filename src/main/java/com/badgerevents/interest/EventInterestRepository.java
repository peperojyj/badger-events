package com.badgerevents.interest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface EventInterestRepository
        extends JpaRepository<EventInterest, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    long countByEventId(Long eventId);

    @Modifying
    @Query("""
            DELETE FROM EventInterest interest
            WHERE interest.user.id = :userId
              AND interest.event.id = :eventId
            """)
    int deleteByUserIdAndEventId(
            @Param("userId") Long userId,
            @Param("eventId") Long eventId
    );

    @Query("""
            SELECT new com.badgerevents.interest.EventInterestCount(
                interest.event.id,
                COUNT(interest)
            )
            FROM EventInterest interest
            WHERE interest.event.id IN :eventIds
            GROUP BY interest.event.id
            """)
    List<EventInterestCount> countByEventIds(
            @Param("eventIds") Collection<Long> eventIds
    );

    @Query("""
            SELECT interest.event.id
            FROM EventInterest interest
            WHERE interest.user.id = :userId
              AND interest.event.id IN :eventIds
            """)
    List<Long> findInterestedEventIds(
            @Param("userId") Long userId,
            @Param("eventIds") Collection<Long> eventIds
    );
}