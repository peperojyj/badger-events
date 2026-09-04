package com.badgerevents.chat;

import java.time.Instant;

public record ChatMembershipResponse(
        boolean joined,
        Instant joinedAt,
        long memberCount
) {

    public static ChatMembershipResponse notJoined(long memberCount) {
        return new ChatMembershipResponse(
                false,
                null,
                memberCount
        );
    }

    public static ChatMembershipResponse from(
            ChatMembership membership,
            long memberCount
    ) {
        return new ChatMembershipResponse(
                true,
                membership.getJoinedAt(),
                memberCount
        );
    }
}
