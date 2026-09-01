package com.badgerevents.eventimport;

import java.time.Instant;

public record ImportRunSummaryResponse(
        Long id,
        ImportRunStatus status,
        Instant startedAt,
        Instant completedAt,
        int createdCount,
        int updatedCount,
        int skippedCount,
        int failedCount,
        String errorMessage
) {

    public static ImportRunSummaryResponse from(
            ImportRun run
    ) {
        return new ImportRunSummaryResponse(
                run.getId(),
                run.getStatus(),
                run.getStartedAt(),
                run.getCompletedAt(),
                run.getCreatedCount(),
                run.getUpdatedCount(),
                run.getSkippedCount(),
                run.getFailedCount(),
                run.getErrorMessage()
        );
    }
}