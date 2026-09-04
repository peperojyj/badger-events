package com.badgerevents.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMembershipRepository
        extends JpaRepository<ChatMembership, Long> {

    Optional<ChatMembership> findByUserIdAndEventId(
            Long userId,
            Long eventId
    );

    @Query("""
            SELECT (COUNT(membership) > 0)
            FROM ChatMembership membership
            WHERE membership.user.email = :email
              AND membership.event.id = :eventId
            """)
    boolean existsByUserEmailAndEventId(
            @Param("email") String email,
            @Param("eventId") Long eventId
    );

    long countByUserIdAndEventId(Long userId, Long eventId);

    long countByEventId(Long eventId);

    @Query("""
            SELECT membership
            FROM ChatMembership membership
            JOIN FETCH membership.user
            WHERE membership.event.id = :eventId
            ORDER BY membership.joinedAt ASC, membership.id ASC
            """)
    List<ChatMembership> findMembersByEventId(
            @Param("eventId") Long eventId
    );
}