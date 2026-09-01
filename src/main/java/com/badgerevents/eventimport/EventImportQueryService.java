package com.badgerevents.eventimport;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EventImportQueryService {

    private final ImportRunRepository importRunRepository;
    private final ImportFailureRepository importFailureRepository;

    public EventImportQueryService(
            ImportRunRepository importRunRepository,
            ImportFailureRepository importFailureRepository
    ) {
        this.importRunRepository = importRunRepository;
        this.importFailureRepository = importFailureRepository;
    }

    public List<ImportRunSummaryResponse> getRuns() {
        return importRunRepository
                .findAllByOrderByStartedAtDesc()
                .stream()
                .map(ImportRunSummaryResponse::from)
                .toList();
    }

    public ImportRunDetailResponse getRun(Long runId) {
        ImportRun run = importRunRepository
                .findById(runId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Import run not found"
                        )
                );

        List<ImportFailureResponse> failures =
                importFailureRepository
                        .findAllByImportRunIdOrderByIdAsc(
                                runId
                        )
                        .stream()
                        .map(ImportFailureResponse::from)
                        .toList();

        return new ImportRunDetailResponse(
                ImportRunSummaryResponse.from(run),
                failures
        );
    }
}