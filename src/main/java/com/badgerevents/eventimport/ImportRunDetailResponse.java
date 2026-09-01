package com.badgerevents.eventimport;

import java.util.List;

public record ImportRunDetailResponse(
        ImportRunSummaryResponse run,
        List<ImportFailureResponse> failures
) {
}