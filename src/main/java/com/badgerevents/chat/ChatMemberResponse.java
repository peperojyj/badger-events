package com.badgerevents.chat;

import java.time.Instant;

// 멤버 공개용 dto
public record ChatMemberResponse(
        Long userId,
        String displayName,
        Instant joinedAt
) {

    public static ChatMemberResponse from(
            ChatMembership membership
    ) {
        return new ChatMemberResponse(
                membership.getUser().getId(),
                membership.getUser().getDisplayName(),
                membership.getJoinedAt()
        );
    }
}