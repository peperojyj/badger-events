package com.badgerevents.eventimport;

public record EventImportResult(
        Long importRunId,
        ImportRunStatus status,
        int createdCount,
        int updatedCount,
        int skippedCount,
        int failedCount,
        String errorMessage
) {

    public static EventImportResult from(ImportRun run) {
        return new EventImportResult(
                run.getId(),
                run.getStatus(),
                run.getCreatedCount(),
                run.getUpdatedCount(),
                run.getSkippedCount(),
                run.getFailedCount(),
                run.getErrorMessage()
        );
    }
}