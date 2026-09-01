package com.badgerevents.eventimport;

import java.time.Instant;

public record ImportFailureResponse(
        Long id,
        String externalId,
        ImportFailureType failureType,
        String failureMessage,
        Instant createdAt
) {

    public static ImportFailureResponse from(
            ImportFailure failure
    ) {
        return new ImportFailureResponse(
                failure.getId(),
                failure.getExternalId(),
                failure.getFailureType(),
                failure.getFailureMessage(),
                failure.getCreatedAt()
        );
    }
}