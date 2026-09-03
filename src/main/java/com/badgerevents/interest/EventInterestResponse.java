package com.badgerevents.interest;

public record EventInterestResponse(
        Long eventId,
        long interestedCount,
        boolean interestedByMe
) {
}